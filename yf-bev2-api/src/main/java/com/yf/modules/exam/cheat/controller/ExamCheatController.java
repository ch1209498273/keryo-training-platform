package com.yf.modules.exam.cheat.controller;

import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.modules.exam.cheat.entity.ExamCheatLog;
import com.yf.modules.exam.cheat.service.ExamCheatLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 考试防切屏控制器（T-05）
 * </p>
 */
@Tag(name = "考试防切屏")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exam/cheat")
public class ExamCheatController extends BaseController {

    private final ExamCheatLogService baseService;

    /**
     * 前端上报切屏/离开页面事件
     *
     * @param reqDTO paperId + examId + content
     * @return
     */
    @Operation(summary = "上报切屏事件")
    @PostMapping("/report")
    public ApiRest<Boolean> report(@RequestBody Map<String, Object> reqDTO) {
        String paperId = (String) reqDTO.getOrDefault("paperId", null);
        String examId = (String) reqDTO.getOrDefault("examId", null);
        String content = (String) reqDTO.getOrDefault("content", "切屏/离开考试页面");
        // 防刷：校验试卷归属（学员只能上报自己试卷的违规）
        if (paperId != null) {
            baseService.checkPaperOwner(paperId);
        }
        baseService.record(paperId, examId, null, null, "CUT_SCREEN", content, null);
        return super.success(true);
    }

    /**
     * 查询某试卷的违规记录（管理端阅卷/复查用）
     *
     * @param reqDTO paperId
     * @return
     */
    @Operation(summary = "查询违规记录")
    @RequiresPermissions("exam:record:list")
    @PostMapping("/list")
    public ApiRest<List<ExamCheatLog>> list(@RequestBody Map<String, Object> reqDTO) {
        String paperId = (String) reqDTO.getOrDefault("paperId", null);
        if (paperId == null) {
            return super.success(List.of());
        }
        return super.success(baseService.listByPaper(paperId));
    }

    /**
     * 学员查询自己试卷的违规记录（考试结果页展示分析报告）
     *
     * @param reqDTO paperId
     * @return
     */
    @Operation(summary = "学员查询自己的违规记录")
    @PostMapping("/list-mine")
    public ApiRest<List<ExamCheatLog>> listMine(@RequestBody Map<String, Object> reqDTO) {
        String paperId = (String) reqDTO.getOrDefault("paperId", null);
        if (paperId == null) {
            return super.success(List.of());
        }
        // IDOR 防护：只能查自己的试卷
        baseService.checkPaperOwner(paperId);
        return super.success(baseService.listByPaper(paperId));
    }
}

