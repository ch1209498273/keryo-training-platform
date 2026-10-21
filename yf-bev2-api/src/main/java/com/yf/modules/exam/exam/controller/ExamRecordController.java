package com.yf.modules.exam.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.base.api.api.dto.PagingReqDTO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yf.base.api.exception.ServiceException;
import com.yf.modules.exam.exam.dto.ExamRecordDTO;
import com.yf.modules.exam.exam.dto.request.ExamRecordListReqDTO;
import com.yf.modules.exam.exam.service.ExamRecordService;
import com.yf.system.modules.user.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "考试记录")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exam/exam/record")
public class ExamRecordController extends BaseController {

    private final ExamRecordService examRecordService;


    /**
     * 分页查找
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "分页查找-管理端", description = "用于管理端，预览考试记录列表")
    @RequiresPermissions("exam:record:list")
    @PostMapping("/paging")
    public ApiRest<IPage<ExamRecordDTO>> paging(@RequestBody PagingReqDTO<ExamRecordListReqDTO> reqDTO) {

        //分页查询并转换
        IPage<ExamRecordDTO> page = examRecordService.paging(reqDTO);

        return super.success(page);
    }


    /**
     * 分页查找
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "分页查找-学员端", description = "用于学员端，查看自己的考试记录")
    @PostMapping("/client-paging")
    public ApiRest<IPage<ExamRecordDTO>> clientPaging(@RequestBody PagingReqDTO<ExamRecordListReqDTO> reqDTO) {

        ExamRecordListReqDTO params = reqDTO.getParams();
        if (params == null) {
            params = new ExamRecordListReqDTO();
        }

        // 限定查找自己的考试记录
        params.setUserId(UserUtils.getUserId());

        //分页查询并转换
        IPage<ExamRecordDTO> page = examRecordService.clientPaging(reqDTO);

        return super.success(page);
    }

    /**
     * 成绩导出 Excel（T-06.2，复用数据权限过滤）
     *
     * @param reqDTO
     * @return xlsx 文件流
     */
    @Operation(summary = "成绩导出Excel", description = "按当前查询条件导出成绩（受数据权限控制）")
    @RequiresPermissions("exam:record:list")
    @PostMapping("/export")
    public void export(@RequestBody PagingReqDTO<ExamRecordListReqDTO> reqDTO, jakarta.servlet.http.HttpServletResponse response) {
        try {
            byte[] data = examRecordService.exportExcel(reqDTO);
            String fileName = "成绩导出_" + System.currentTimeMillis() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new ServiceException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 成绩修正（权限模型 v2：仅医护总监专属，人工仲裁）
     * 修改最高分/最近分/通过状态，并写入审计日志；若成绩已锁定(locked=1)自动解锁覆盖
     */
    @Operation(summary = "成绩修正（总监专属）", description = "人工仲裁修正学员成绩，写审计日志")
    @RequiresPermissions("exam:record:edit")
    @PostMapping("/edit-score")
    public ApiRest<?> editScore(@RequestBody java.util.Map<String, Object> req) {
        String recordId = (String) req.get("recordId");
        Object scoreObj = req.get("score");
        String reason = (String) req.getOrDefault("reason", "");
        if (recordId == null || scoreObj == null) {
            throw new ServiceException("参数不完整：recordId/score 必填");
        }
        java.math.BigDecimal score = new java.math.BigDecimal(String.valueOf(scoreObj));
        examRecordService.editScore(recordId, score, reason);
        return super.success();
    }

    /**
     * 补考（管理端发起）：重置该学员该考试的已考次数，允许再次进入考试
     * 保留历史成绩，补考交卷后按新成绩更新最近分/通过状态，不重复发放学分
     */
    @Operation(summary = "补考（管理端发起）", description = "重置学员已考次数，允许再次参加考试")
    @RequiresPermissions("exam:record:retry")
    @PostMapping("/retry")
    public ApiRest<?> retry(@RequestBody java.util.Map<String, Object> req) {
        String examId = (String) req.get("examId");
        String userId = (String) req.get("userId");
        if (examId == null || userId == null) {
            throw new ServiceException("参数不完整：examId/userId 必填");
        }
        examRecordService.retryExam(examId, userId);
        return super.success();
    }

}

