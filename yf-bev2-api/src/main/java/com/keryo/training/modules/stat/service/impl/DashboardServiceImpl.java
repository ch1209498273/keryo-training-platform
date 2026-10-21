package com.keryo.training.modules.stat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keryo.training.modules.stat.entity.DashboardDTO;
import com.keryo.training.modules.stat.service.DashboardService;
import com.keryo.training.modules.audit.util.DesensitizeUtil;
import com.keryo.training.modules.trainingplan.entity.PlanUser;
import com.keryo.training.modules.trainingplan.entity.TrainingPlan;
import com.keryo.training.modules.trainingplan.mapper.PlanUserMapper;
import com.keryo.training.modules.trainingplan.mapper.TrainingPlanMapper;
import com.yf.ability.shiro.dto.SysUserLoginDTO;
import com.yf.base.enums.DataScope;
import com.yf.modules.exam.exam.entity.Exam;
import com.yf.modules.exam.exam.mapper.ExamMapper;
import com.yf.modules.exam.exam.mapper.ExamRecordMapper;
import com.yf.modules.exam.exam.entity.ExamRecord;
import com.yf.system.modules.user.UserUtils;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.mapper.SysUserMapper;
import com.yf.system.modules.depart.entity.SysDepart;
import com.yf.system.modules.depart.mapper.SysDepartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计看板实现（T-10）
 * 管理端：data_scope 过滤（医护长看本科室、超管/助理全量）
 * 学员端：只看自己
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String NO_SCOPE = "__NO_DATA_SCOPE__";

    private final ExamRecordMapper examRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final ExamMapper examMapper;
    private final SysDepartMapper sysDepartMapper;
    private final TrainingPlanMapper trainingPlanMapper;
    private final PlanUserMapper planUserMapper;

    @Override
    public DashboardDTO dashboard() {
        DashboardDTO dto = new DashboardDTO();
        List<String> scopeUserIds = scopeUserIds();

        // 卡片：学员总数（启用）
        dto.setStudentCount(sysUserMapper.selectCount(
                new QueryWrapper<SysUser>().eq("state", 0)
                        .in(!scopeUserIds.isEmpty() && !isAllScope(), "id", scopeUserIds)));

        // 成绩记录（权限范围内）
        List<ExamRecord> records = recordList(scopeUserIds);
        dto.setCompletedCount((long) records.size());
        dto.setAvgScore(records.stream().map(ExamRecord::getMaxScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(records.isEmpty() ? BigDecimal.ONE : BigDecimal.valueOf(records.size()),
                        2, RoundingMode.HALF_UP));
        dto.setAnnualTrainingCount(annualTrainingCount(scopeUserIds));

        // 通过率折线：按考试分组
        List<DashboardDTO.PassRatePoint> trend = new ArrayList<>();
        Map<String, List<ExamRecord>> byExam = records.stream()
                .collect(Collectors.groupingBy(ExamRecord::getExamId));
        byExam.forEach((examId, list) -> {
            DashboardDTO.PassRatePoint p = new DashboardDTO.PassRatePoint();
            p.setName(examTitle(examId));
            p.setTotal(list.size());
            p.setPassed((int) list.stream().filter(r -> Boolean.TRUE.equals(r.getPassed())).count());
            p.setRate(BigDecimal.valueOf(p.getPassed() * 100.0 / p.getTotal()).setScale(1, RoundingMode.HALF_UP));
            trend.add(p);
        });
        trend.sort(Comparator.comparing(DashboardDTO.PassRatePoint::getName));
        dto.setPassRateTrend(trend);

        // 参训名单（最近20条）
        dto.setTrainees(toItems(records.stream().limit(20).collect(Collectors.toList())));

        // 未完成：有资格（状态启用 + 在 scope）但无成绩
        dto.setNotFinished(notFinishedUsers(scopeUserIds, records));
        return dto;
    }

    @Override
    public DashboardDTO myOverview() {
        DashboardDTO dto = new DashboardDTO();
        String userId = UserUtils.getUserId();
        List<ExamRecord> records = examRecordMapper.selectList(
                new QueryWrapper<ExamRecord>().eq("user_id", userId)
                        .orderByDesc("create_time"));
        dto.setCompletedCount((long) records.size());
        dto.setAvgScore(records.stream().map(ExamRecord::getMaxScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(records.isEmpty() ? BigDecimal.ONE : BigDecimal.valueOf(records.size()),
                        2, RoundingMode.HALF_UP));
        dto.setAnnualTrainingCount(annualTrainingCount(List.of(userId)));
        dto.setPassRateTrend(records.stream().map(r -> {
            DashboardDTO.PassRatePoint p = new DashboardDTO.PassRatePoint();
            p.setName(examTitle(r.getExamId()));
            p.setTotal(1);
            p.setPassed(Boolean.TRUE.equals(r.getPassed()) ? 1 : 0);
            p.setRate(Integer.valueOf(1).equals(p.getPassed())
                    ? new BigDecimal("100.0") : BigDecimal.ZERO);
            return p;
        }).collect(Collectors.toList()));
        dto.setTrainees(toItems(records));
        return dto;
    }

    // ---------- 私有 ----------

    /** 权限范围内用户ID（空=全量；NO_SCOPE=明确无数据权限）。 */
    private List<String> scopeUserIds() {
        Integer scope = UserUtils.getDataScope();
        if (scope == null || scope == DataScope.SCOPE_ALL) {
            return List.of();
        }
        QueryWrapper<SysUser> q = new QueryWrapper<SysUser>().eq("state", 0);
        if (scope == DataScope.SCOPE_SELF) {
            q.eq("id", UserUtils.getUserId());
        } else if (scope == DataScope.SCOPE_DEPT) {
            String deptCode = UserUtils.departCode();
            if (deptCode == null || deptCode.isBlank()) {
                return List.of(NO_SCOPE);
            }
            q.eq("dept_code", deptCode);
        } else if (scope == DataScope.SCOPE_DEPT_DOWN) {
            // 区域督导：area_code 下所有中心科室的用户
            SysUserLoginDTO user = UserUtils.getUser(false);
            if (user == null || user.getAreaCode() == null || user.getAreaCode().isBlank()) {
                return List.of(NO_SCOPE);
            }
            List<String> deptCodes = sysDepartMapper.selectList(
                    new QueryWrapper<SysDepart>().eq("area_code", user.getAreaCode()))
                    .stream().map(SysDepart::getDeptCode).collect(Collectors.toList());
            if (deptCodes.isEmpty()) {
                return List.of(NO_SCOPE);
            }
            q.in("dept_code", deptCodes);
        }
        List<String> ids = sysUserMapper.selectList(q).stream()
                .map(SysUser::getId).filter(Objects::nonNull).collect(Collectors.toList());
        return ids.isEmpty() ? List.of(NO_SCOPE) : ids;
    }

    private boolean isAllScope() {
        Integer scope = UserUtils.getDataScope();
        return scope != null && scope == DataScope.SCOPE_ALL;
    }

    private List<ExamRecord> recordList(List<String> scopeUserIds) {
        QueryWrapper<ExamRecord> q = new QueryWrapper<ExamRecord>()
                .orderByDesc("create_time");
        if (!scopeUserIds.isEmpty()) {
            q.in("user_id", scopeUserIds);
        }
        return examRecordMapper.selectList(q);
    }

    /**
     * Count plan assignments overlapping the current calendar year. A plan
     * without explicit dates falls back to its creation time for the year.
     */
    private long annualTrainingCount(List<String> scopeUserIds) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date yearStart = calendar.getTime();
        calendar.add(Calendar.YEAR, 1);
        Date nextYear = calendar.getTime();

        QueryWrapper<TrainingPlan> planQuery = new QueryWrapper<>();
        planQuery.apply("COALESCE(start_time, create_time) < {0} "
                + "AND COALESCE(end_time, start_time, create_time) >= {1}", nextYear, yearStart);
        List<String> planIds = trainingPlanMapper.selectList(planQuery).stream()
                .map(TrainingPlan::getId).filter(Objects::nonNull).collect(Collectors.toList());
        if (planIds.isEmpty()) {
            return 0L;
        }

        QueryWrapper<PlanUser> participantQuery = new QueryWrapper<PlanUser>()
                .in("plan_id", planIds);
        if (!scopeUserIds.isEmpty()) {
            participantQuery.in("user_id", scopeUserIds);
        }
        return planUserMapper.selectCount(participantQuery);
    }

    private String examTitle(String examId) {
        if (examId == null) return "未知考试";
        Exam e = examMapper.selectById(examId);
        return e == null ? "未知考试" : e.getTitle();
    }

    private String deptName(String deptCode) {
        if (deptCode == null || deptCode.isBlank()) return "";
        QueryWrapper<SysDepart> q = new QueryWrapper<SysDepart>().eq("dept_code", deptCode).last("LIMIT 1");
        SysDepart d = sysDepartMapper.selectOne(q);
        return d == null ? "" : d.getDeptName();
    }

    private List<DashboardDTO.TraineeItem> toItems(List<ExamRecord> records) {
        List<DashboardDTO.TraineeItem> items = new ArrayList<>();
        for (ExamRecord r : records) {
            DashboardDTO.TraineeItem it = new DashboardDTO.TraineeItem();
            it.setUserId(r.getUserId());
            it.setExamTitle(examTitle(r.getExamId()));
            it.setScore(r.getMaxScore());
            it.setPassed(Boolean.TRUE.equals(r.getPassed()) ? 1 : 0);
            it.setHandTime(r.getCreateTime() == null ? "" : String.valueOf(r.getCreateTime()));
            SysUser u = sysUserMapper.selectById(r.getUserId());
            if (u != null) {
                it.setRealName(DesensitizeUtil.name(u.getRealName()));
                it.setDeptName(deptName(u.getDeptCode()));
            }
            items.add(it);
        }
        return items;
    }

    private List<DashboardDTO.TraineeItem> notFinishedUsers(List<String> scopeUserIds, List<ExamRecord> records) {
        if (scopeUserIds.contains(NO_SCOPE)) {
            return List.of();
        }
        Set<String> done = records.stream()
                .filter(record -> record.getUserId() != null && record.getExamId() != null)
                .map(record -> record.getUserId() + "\u0000" + record.getExamId())
                .collect(Collectors.toSet());
        List<TrainingPlan> activePlans = trainingPlanMapper.selectList(new QueryWrapper<TrainingPlan>()
                .select("id", "exam_id").eq("status", "ACTIVE").isNotNull("exam_id"));
        if (activePlans.isEmpty()) {
            return List.of();
        }
        Map<String, String> examByPlan = activePlans.stream()
                .filter(plan -> plan.getId() != null && plan.getExamId() != null)
                .collect(Collectors.toMap(TrainingPlan::getId, TrainingPlan::getExamId, (left, right) -> left));
        if (examByPlan.isEmpty()) {
            return List.of();
        }
        QueryWrapper<PlanUser> participantQuery = new QueryWrapper<PlanUser>()
                .select("user_id", "plan_id").in("plan_id", examByPlan.keySet());
        if (!scopeUserIds.isEmpty()) {
            participantQuery.in("user_id", scopeUserIds);
        }
        Map<String, Set<String>> requiredExamsByUser = new HashMap<>();
        for (PlanUser assignment : planUserMapper.selectList(participantQuery)) {
            String userId = assignment.getUserId();
            String examId = examByPlan.get(assignment.getPlanId());
            if (userId != null && examId != null) {
                requiredExamsByUser.computeIfAbsent(userId, ignored -> new HashSet<>()).add(examId);
            }
        }
        if (requiredExamsByUser.isEmpty()) {
            return List.of();
        }
        QueryWrapper<SysUser> q = new QueryWrapper<SysUser>().eq("state", 0);
        q.in("id", requiredExamsByUser.keySet());
        List<DashboardDTO.TraineeItem> items = new ArrayList<>();
        for (SysUser u : sysUserMapper.selectList(q)) {
            Set<String> requiredExams = requiredExamsByUser.getOrDefault(u.getId(), Set.of());
            boolean incomplete = requiredExams.stream()
                    .anyMatch(examId -> !done.contains(u.getId() + "\u0000" + examId));
            if (!incomplete) continue;
            DashboardDTO.TraineeItem it = new DashboardDTO.TraineeItem();
            it.setUserId(u.getId());
            it.setRealName(DesensitizeUtil.name(u.getRealName()));
            it.setDeptName(deptName(u.getDeptCode()));
            it.setExamTitle("未参加任何考试");
            items.add(it);
        }
        return items.stream().limit(20).collect(Collectors.toList());
    }
}

