package com.keryo.training.modules.trainingplan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keryo.training.modules.trainingplan.entity.TrainingPlan;
import com.keryo.training.modules.audit.annotation.AuditLog;
import com.keryo.training.modules.trainingplan.service.TrainingPlanService;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.base.api.api.dto.PagingReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 培训计划（T-06.3）
 */
@Tag(name = "培训计划")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keryo/training-plan")
@RequiresAuthentication
public class TrainingPlanController extends BaseController {

    private final TrainingPlanService trainingPlanService;

    /** 学员参与 Mapper（T-13b） */
    @jakarta.annotation.Resource
    private com.keryo.training.modules.trainingplan.mapper.PlanUserMapper planUserMapper;

    /** 用户 Mapper（T-13b） */
    @jakarta.annotation.Resource
    private com.yf.system.modules.user.mapper.SysUserMapper sysUserMapper;

    @Operation(summary = "分页查询")
    @PostMapping("/paging")
    public ApiRest<IPage<TrainingPlan>> paging(@RequestBody PagingReqDTO<TrainingPlan> reqDTO) {
        return super.success(trainingPlanService.paging(reqDTO));
    }

    @Operation(summary = "详情")
    @PostMapping("/detail")
    public ApiRest<TrainingPlan> detail(@RequestBody Map<String, Object> req) {
        return super.success(trainingPlanService.getAccessible((String) req.get("id")));
    }

    @Operation(summary = "保存（DRAFT）")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @PostMapping("/save")
    public ApiRest<String> save(@RequestBody TrainingPlan plan) {
        trainingPlanService.savePlan(plan);
        return super.success(plan.getId());
    }

    @Operation(summary = "删除")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @PostMapping("/delete")
    public ApiRest<?> delete(@RequestBody Map<String, Object> req) {
        trainingPlanService.deletePlan((String) req.get("id"));
        return super.success();
    }

    @Operation(summary = "提交审批")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @AuditLog(module = "培训计划", operation = "提交审批")
    @PostMapping("/submit")
    public ApiRest<?> submit(@RequestBody Map<String, Object> req) {
        trainingPlanService.submit((String) req.get("id"));
        return super.success();
    }

    @Operation(summary = "审批通过")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @AuditLog(module = "培训计划", operation = "审批通过")
    @PostMapping("/approve")
    public ApiRest<?> approve(@RequestBody Map<String, Object> req) {
        trainingPlanService.approve((String) req.get("id"), (String) req.get("remark"));
        return super.success();
    }

    @Operation(summary = "审批驳回")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @AuditLog(module = "培训计划", operation = "审批驳回")
    @PostMapping("/reject")
    public ApiRest<?> reject(@RequestBody Map<String, Object> req) {
        trainingPlanService.reject((String) req.get("id"), (String) req.get("remark"));
        return super.success();
    }

    @Operation(summary = "归档")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @PostMapping("/archive")
    public ApiRest<?> archive(@RequestBody Map<String, Object> req) {
        trainingPlanService.archive((String) req.get("id"));
        return super.success();
    }

    // ===== T-13b 学员参与管理 =====

    /** 计划下的参与学员列表 */
    @Operation(summary = "计划参与学员列表")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant", "keryo_team_leader"}, logical = Logical.OR)
    @PostMapping("/participants")
    public ApiRest<java.util.List<Map<String, Object>>> participants(@RequestBody Map<String, Object> req) {
        String planId = (String) req.get("planId");
        trainingPlanService.checkParticipantAccess(planId, java.util.List.of());
        java.util.List<com.keryo.training.modules.trainingplan.entity.PlanUser> pus =
                planUserMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                        com.keryo.training.modules.trainingplan.entity.PlanUser>()
                        .eq(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId, planId));
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var pu : pus) {
            var u = sysUserMapper.selectById(pu.getUserId());
            if (u == null) {
                continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("userId", u.getId());
            m.put("realName", u.getRealName());
            m.put("userName", u.getUserName());
            result.add(m);
        }
        return super.success(result);
    }

    /** 当前操作者可分配的启用用户；小组长限定为本部门。 */
    @Operation(summary = "可分配学员候选列表")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant", "keryo_team_leader"}, logical = Logical.OR)
    @PostMapping("/participant-candidates")
    public ApiRest<java.util.List<Map<String, Object>>> participantCandidates(
            @RequestBody Map<String, Object> req) {
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.yf.system.modules.user.entity.SysUser user :
                trainingPlanService.participantCandidates((String) req.get("planId"))) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("userName", user.getUserName());
            item.put("realName", user.getRealName());
            item.put("deptCode", user.getDeptCode());
            result.add(item);
        }
        return super.success(result);
    }

    /** 分配学员参与（批量 userIds） */
    @Operation(summary = "分配学员参与计划")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant", "keryo_team_leader"}, logical = Logical.OR)
    @PostMapping("/assign")
    public ApiRest<?> assign(@RequestBody Map<String, Object> req) {
        String planId = (String) req.get("planId");
        @SuppressWarnings("unchecked")
        java.util.List<String> userIds = (java.util.List<String>) req.get("userIds");
        if (planId == null || userIds == null || userIds.isEmpty()) {
            throw new com.yf.base.api.exception.ServiceException("参数不完整：planId/userIds 必填");
        }
        trainingPlanService.checkParticipantAccess(planId, userIds);
        java.util.Date now = new java.util.Date();
        for (String uid : userIds) {
            Long cnt = planUserMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                    com.keryo.training.modules.trainingplan.entity.PlanUser>()
                    .eq(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId, planId)
                    .eq(com.keryo.training.modules.trainingplan.entity.PlanUser::getUserId, uid));
            if (cnt != null && cnt > 0) {
                continue;
            }
            com.keryo.training.modules.trainingplan.entity.PlanUser pu =
                    new com.keryo.training.modules.trainingplan.entity.PlanUser();
            pu.setPlanId(planId);
            pu.setUserId(uid);
            pu.setCreateTime(now);
            planUserMapper.insert(pu);
        }
        return super.success();
    }

    /** 移除学员参与 */
    @Operation(summary = "移除学员参与")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant", "keryo_team_leader"}, logical = Logical.OR)
    @PostMapping("/remove-user")
    public ApiRest<?> removeUser(@RequestBody Map<String, Object> req) {
        String planId = (String) req.get("planId");
        String userId = (String) req.get("userId");
        if (planId == null || planId.isBlank() || userId == null || userId.isBlank()) {
            throw new com.yf.base.api.exception.ServiceException("参数不完整：planId/userId 必填");
        }
        trainingPlanService.checkParticipantAccess(planId, java.util.List.of(userId));
        planUserMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                com.keryo.training.modules.trainingplan.entity.PlanUser>()
                .eq(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId, planId)
                .eq(com.keryo.training.modules.trainingplan.entity.PlanUser::getUserId, userId));
        return super.success();
    }
}

