package com.keryo.training.modules.trainingplan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keryo.training.modules.trainingplan.entity.TrainingPlan;
import com.keryo.training.modules.trainingplan.mapper.TrainingPlanMapper;
import com.keryo.training.modules.trainingplan.mapper.PlanUserMapper;
import com.keryo.training.modules.trainingplan.service.TrainingPlanService;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import com.yf.ability.shiro.dto.SysUserLoginDTO;
import com.yf.system.modules.user.UserUtils;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 培训计划（T-06.3）
 * 状态机：DRAFT → PENDING → ACTIVE → ENDED → ARCHIVED（PENDING 可驳回回 DRAFT）
 */
@Log4j2
@Service
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan>
        implements TrainingPlanService {

    @jakarta.annotation.Resource
    private SysUserMapper sysUserMapper;

    @jakarta.annotation.Resource
    private PlanUserMapper planUserMapper;

    @Override
    public IPage<TrainingPlan> paging(PagingReqDTO<TrainingPlan> reqDTO) {
        LambdaQueryWrapper<TrainingPlan> q = new LambdaQueryWrapper<>();
        TrainingPlan params = reqDTO.getParams();
        if (params != null) {
            if (StringUtils.hasText(params.getTitle())) {
                q.like(TrainingPlan::getTitle, params.getTitle());
            }
            if (StringUtils.hasText(params.getStatus())) {
                q.eq(TrainingPlan::getStatus, params.getStatus());
            }
        }
        // 数据权限：医护长看本科室+全院；助理管理员/超管看全部；学员看自己的（学员一般看不到计划管理，防御性处理）
        List<String> roles = UserUtils.getRoles();
        boolean isManager = roles != null && (roles.contains("助理管理员") || roles.contains("admin")
                || roles.contains("keryo_assistant") || roles.contains("keryo_director")
                || roles.contains("超管"));
        if (!isManager) {
            SysUserLoginDTO user = UserUtils.getUser(false);
            if (user != null && StringUtils.hasText(user.getDeptCode())) {
                // 本科室 + 全院计划
                q.and(w -> w.eq(TrainingPlan::getDeptCode, user.getDeptCode())
                        .or().isNull(TrainingPlan::getDeptCode));
                if (!isTeamLeader()) {
                    List<String> participantPlanIds = this.planIdsForUser(user.getId());
                    q.and(w -> w.in(TrainingPlan::getStatus, "ACTIVE", "ENDED")
                            .in(TrainingPlan::getId, participantPlanIds.isEmpty()
                                    ? List.of("__NO_PLAN_ACCESS__") : participantPlanIds));
                }
            } else {
                q.apply("1 = 0");
            }
        }
        q.orderByDesc(TrainingPlan::getCreateTime);
        return this.page(new Page<>(reqDTO.getCurrent(), reqDTO.getSize()), q);
    }

    @Override
    public void savePlan(TrainingPlan plan) {
        if (plan == null) {
            throw new ServiceException("培训计划不能为空");
        }
        String currentUserId = UserUtils.getUserId();
        if (StringUtils.hasText(plan.getId())) {
            TrainingPlan old = this.getById(plan.getId());
            if (old != null && !"DRAFT".equals(old.getStatus())) {
                throw new ServiceException("只有草稿(DRAFT)状态的计划才能修改！");
            }
            if (old != null && !isManager() && !currentUserId.equals(old.getCreateBy())) {
                throw new ServiceException("只能修改自己创建的培训计划");
            }
            if (old != null) {
                // 客户端不能通过更新请求重置创建人和创建时间。
                plan.setCreateBy(old.getCreateBy());
                plan.setCreateTime(old.getCreateTime());
            }
        }
        plan.setStatus("DRAFT");
        if (!StringUtils.hasText(plan.getCreateBy())) {
            plan.setCreateBy(currentUserId);
        }
        if (plan.getCreateTime() == null) {
            plan.setCreateTime(new Date());
        }
        plan.setUpdateBy(currentUserId);
        plan.setUpdateTime(new Date());
        this.saveOrUpdate(plan);
    }

    @Override
    public TrainingPlan getAccessible(String id) {
        TrainingPlan plan = this.requirePlan(id);
        if (isManager()) {
            return plan;
        }
        SysUserLoginDTO user = UserUtils.getUser(true);
        if (isTeamLeader() && StringUtils.hasText(user.getDeptCode())
                && user.getDeptCode().equals(plan.getDeptCode())) {
            return plan;
        }
        if (("ACTIVE".equals(plan.getStatus()) || "ENDED".equals(plan.getStatus()))
                && this.planIdsForUser(user.getId()).contains(plan.getId())) {
            return plan;
        }
        throw new ServiceException("无权查看该培训计划");
    }

    @Override
    public void deletePlan(String id) {
        TrainingPlan plan = this.requirePlan(id);
        if (!"DRAFT".equals(plan.getStatus())) {
            throw new ServiceException("只有草稿(DRAFT)状态的计划才能删除");
        }
        if (!isManager() && !UserUtils.getUserId().equals(plan.getCreateBy())) {
            throw new ServiceException("只能删除自己创建的培训计划");
        }
        this.removeById(id);
    }

    @Override
    public void submit(String id) {
        this.checkManager();
        TrainingPlan plan = this.requirePlan(id);
        if (!"DRAFT".equals(plan.getStatus())) {
            throw new ServiceException("只有草稿(DRAFT)状态才能提交审批！");
        }
        plan.setStatus("PENDING");
        plan.setSubmitBy(UserUtils.getUserId());
        plan.setSubmitTime(new Date());
        this.updateById(plan);
    }

    @Override
    public void approve(String id, String remark) {
        this.checkManager();
        TrainingPlan plan = this.requirePlan(id);
        if (!"PENDING".equals(plan.getStatus())) {
            throw new ServiceException("只有待审批(PENDING)状态才能通过！");
        }
        plan.setStatus("ACTIVE");
        plan.setApproveBy(UserUtils.getUserId());
        plan.setApproveTime(new Date());
        plan.setApproveRemark(remark);
        this.updateById(plan);
        log.info("培训计划[{}]审批通过", plan.getTitle());
    }

    @Override
    public void reject(String id, String remark) {
        this.checkManager();
        TrainingPlan plan = this.requirePlan(id);
        if (!"PENDING".equals(plan.getStatus())) {
            throw new ServiceException("只有待审批(PENDING)状态才能驳回！");
        }
        plan.setStatus("DRAFT");
        plan.setApproveBy(UserUtils.getUserId());
        plan.setApproveTime(new Date());
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }

    @Override
    public void archive(String id) {
        this.checkManager();
        TrainingPlan plan = this.requirePlan(id);
        if (!"ENDED".equals(plan.getStatus()) && !"ACTIVE".equals(plan.getStatus())) {
            throw new ServiceException("只有进行中(ACTIVE)或已结束(ENDED)的计划才能归档！");
        }
        plan.setStatus("ARCHIVED");
        this.updateById(plan);
    }

    @Override
    @Scheduled(fixedDelayString = "${keryo.training-plan.auto-end-delay-ms:60000}")
    public void autoEnd() {
        // 到 end_time 的 ACTIVE 计划 → ENDED
        LambdaQueryWrapper<TrainingPlan> q = new LambdaQueryWrapper<>();
        q.eq(TrainingPlan::getStatus, "ACTIVE").lt(TrainingPlan::getEndTime, new Date());
        List<TrainingPlan> list = this.list(q);
        for (TrainingPlan plan : list) {
            plan.setStatus("ENDED");
            this.updateById(plan);
            log.info("培训计划[{}]自动到期结束", plan.getTitle());
        }
    }

    @Override
    public void checkParticipantAccess(String planId, List<String> userIds) {
        TrainingPlan plan = this.requirePlan(planId);
        if (isManager()) {
            return;
        }
        if (!isTeamLeader()) {
            throw new ServiceException("无权管理培训计划参与关系");
        }
        SysUserLoginDTO user = UserUtils.getUser(false);
        if (user == null || !StringUtils.hasText(user.getDeptCode())
                || !StringUtils.hasText(plan.getDeptCode())
                || !plan.getDeptCode().equals(user.getDeptCode())) {
            throw new ServiceException("小组长只能管理本部门且已绑定部门的培训计划");
        }
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        Set<String> distinctIds = new HashSet<>(userIds);
        if (distinctIds.stream().anyMatch(id -> !StringUtils.hasText(id))) {
            throw new ServiceException("学员ID不能为空");
        }
        List<SysUser> users = sysUserMapper.selectList(new QueryWrapper<SysUser>()
                .eq("state", 0).in("id", distinctIds));
        if (users.size() != distinctIds.size()
                || users.stream().anyMatch(candidate -> !user.getDeptCode().equals(candidate.getDeptCode()))) {
            throw new ServiceException("小组长只能分配本部门的启用学员");
        }
    }

    @Override
    public List<SysUser> participantCandidates(String planId) {
        checkParticipantAccess(planId, List.of());
        TrainingPlan plan = this.requirePlan(planId);
        QueryWrapper<SysUser> query = new QueryWrapper<SysUser>().eq("state", 0)
                .orderByAsc("dept_code").orderByAsc("real_name");
        if (isTeamLeader()) {
            query.eq("dept_code", UserUtils.departCode());
        } else if (StringUtils.hasText(plan.getDeptCode())) {
            query.eq("dept_code", plan.getDeptCode());
        }
        return sysUserMapper.selectList(query);
    }

    private TrainingPlan requirePlan(String id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new ServiceException("培训计划不存在！");
        }
        return plan;
    }

    private void checkManager() {
        if (!isManager()) {
            throw new ServiceException("无权限执行该操作，仅助理管理员可审批！");
        }
    }

    private boolean isManager() {
        List<String> roles = UserUtils.getRoles();
        return roles != null && (roles.contains("助理管理员") || roles.contains("admin")
                || roles.contains("keryo_assistant") || roles.contains("keryo_director")
                || roles.contains("超管"));
    }

    private boolean isTeamLeader() {
        List<String> roles = UserUtils.getRoles();
        return roles != null && roles.contains("keryo_team_leader");
    }

    private List<String> planIdsForUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            return List.of();
        }
        return planUserMapper.selectList(new QueryWrapper<com.keryo.training.modules.trainingplan.entity.PlanUser>()
                        .eq("user_id", userId))
                .stream()
                .map(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId)
                .filter(StringUtils::hasText)
                .toList();
    }
}

