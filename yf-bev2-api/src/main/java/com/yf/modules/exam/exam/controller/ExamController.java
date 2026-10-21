package com.yf.modules.exam.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.base.api.api.dto.BaseIdReqDTO;
import com.yf.base.api.api.dto.BaseIdsReqDTO;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import com.yf.modules.exam.exam.dto.ExamDTO;
import com.yf.modules.exam.exam.dto.request.ExamListReqDTO;
import com.yf.modules.exam.exam.dto.response.ExamDetailDTO;
import com.yf.modules.exam.exam.service.ExamService;
import com.yf.system.modules.user.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 考试控制器
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-14 17:29
 */
@Tag(name = "考试管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exam/exam/exam")
@RequiresAuthentication
public class ExamController extends BaseController {

    private final ExamService examService;

    /** 学员参与 Mapper（学员端考试按培训计划过滤） */
    @jakarta.annotation.Resource
    private com.keryo.training.modules.trainingplan.mapper.PlanUserMapper planUserMapper;

    /** 培训计划 Mapper（学员端考试按培训计划过滤） */
    @jakarta.annotation.Resource
    private com.keryo.training.modules.trainingplan.mapper.TrainingPlanMapper trainingPlanMapper;

    /**
     * 添加或修改
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "添加或修改")
    @RequiresPermissions(value = {"exam:exam:add", "exam:exam:edit"}, logical = Logical.OR)
    @PostMapping("/save")
    public ApiRest<?> save(@RequestBody ExamDetailDTO reqDTO) {
        examService.save(reqDTO);
        return super.success();
    }

    /**
     * 批量删除
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "批量删除")
    @RequiresPermissions("exam:exam:delete")
    @PostMapping("/delete")
    public ApiRest<?> delete(@RequestBody BaseIdsReqDTO reqDTO) {
        //根据ID删除
        examService.delete(reqDTO.getIds());
        return super.success();
    }

    /**
     * 查找详情
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "查找详情")
    @RequiresPermissions("exam:exam:view")
    @PostMapping("/detail")
    public ApiRest<ExamDetailDTO> detail(@RequestBody BaseIdReqDTO reqDTO) {
        ExamDetailDTO dto = examService.detail(reqDTO.getId());
        return super.success(dto);
    }

    /**
     * 分页查找
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "分页查找")
    @RequiresPermissions("exam:exam:view")
    @PostMapping("/paging")
    public ApiRest<IPage<ExamDTO>> paging(@RequestBody PagingReqDTO<ExamListReqDTO> reqDTO) {

        //分页查询并转换
        IPage<ExamDTO> page = examService.paging(reqDTO);

        return super.success(page);
    }


    /**
     * 详情用于考试
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "详情用于考试")
    @PostMapping("/detail-for-exam")
    public ApiRest<ExamDetailDTO> detailForExam(@RequestBody BaseIdReqDTO reqDTO) {
        assertExamParticipant(reqDTO.getId());
        ExamDetailDTO dto = examService.detail(reqDTO.getId());
        return super.success(dto);
    }

    /**
     * 分页查找
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "分页查找")
    @PostMapping("/client-paging")
    public ApiRest<IPage<ExamDTO>> clientPaging(@RequestBody PagingReqDTO<ExamListReqDTO> reqDTO) {
        // 学员端可见范围：只返回当前学员参与的培训计划（ACTIVE/ENDED）所关联的考试
        String userId = UserUtils.getUserId();
        java.util.List<com.keryo.training.modules.trainingplan.entity.PlanUser> pus =
                planUserMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                        com.keryo.training.modules.trainingplan.entity.PlanUser>()
                        .eq(com.keryo.training.modules.trainingplan.entity.PlanUser::getUserId, userId));
        if (pus.isEmpty()) {
            return success(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        }
        java.util.List<String> planIds = pus.stream()
                .map(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId)
                .toList();
        java.util.List<com.keryo.training.modules.trainingplan.entity.TrainingPlan> plans =
                trainingPlanMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                        com.keryo.training.modules.trainingplan.entity.TrainingPlan>()
                        .in(com.keryo.training.modules.trainingplan.entity.TrainingPlan::getId, planIds)
                        .in(com.keryo.training.modules.trainingplan.entity.TrainingPlan::getStatus, "ACTIVE", "ENDED")
                        .isNotNull(com.keryo.training.modules.trainingplan.entity.TrainingPlan::getExamId));
        java.util.List<String> examIds = plans.stream()
                .map(com.keryo.training.modules.trainingplan.entity.TrainingPlan::getExamId)
                .distinct()
                .toList();
        if (examIds.isEmpty()) {
            return success(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        }

        // 设置可见范围后走通用分页
        if (reqDTO.getParams() == null) {
            reqDTO.setParams(new ExamListReqDTO());
        }
        reqDTO.getParams().setExamIds(examIds);
        IPage<ExamDTO> page = examService.paging(reqDTO);
        return super.success(page);
    }

    /**
     * 考试详情包含题目/组卷信息，不能只依赖前端考试列表过滤。
     * 只有 ACTIVE/ENDED 计划的实际参与者才能进入考试详情接口。
     */
    private void assertExamParticipant(String examId) {
        if (examId == null || examId.isBlank()) {
            throw new ServiceException("考试 ID 不能为空");
        }
        String userId = UserUtils.getUserId();
        java.util.List<com.keryo.training.modules.trainingplan.entity.PlanUser> assignments =
                planUserMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                        com.keryo.training.modules.trainingplan.entity.PlanUser>()
                        .eq("user_id", userId));
        java.util.List<String> planIds = assignments.stream()
                .map(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (planIds.isEmpty()) {
            throw new ServiceException("无权查看该考试");
        }
        Long count = trainingPlanMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                com.keryo.training.modules.trainingplan.entity.TrainingPlan>()
                .eq("exam_id", examId)
                .in("id", planIds)
                .in("status", java.util.List.of("ACTIVE", "ENDED")));
        if (count == null || count == 0) {
            throw new ServiceException("无权查看该考试");
        }
    }

    /**
     * 成绩公示切换（管理端）：0=不公示（学员交卷后看不到分数，显示待公布）/1=公示
     * 可随时切换，切换后学员端立即生效
     */
    @Operation(summary = "成绩公示切换", description = "管理端控制该考试成绩是否向学员公布")
    @RequiresPermissions("exam:exam:edit")
    @PostMapping("/score-visible")
    public ApiRest<?> setScoreVisible(@RequestBody java.util.Map<String, Object> req) {
        String examId = (String) req.get("examId");
        Object visibleObj = req.get("scoreVisible");
        if (examId == null || visibleObj == null) {
            throw new ServiceException("参数不完整：examId/scoreVisible 必填");
        }
        Integer visible = Integer.valueOf(String.valueOf(visibleObj));
        if (!visible.equals(0) && !visible.equals(1)) {
            throw new ServiceException("scoreVisible 仅支持 0/1");
        }
        examService.setScoreVisible(examId, visible);
        return super.success();
    }
}

