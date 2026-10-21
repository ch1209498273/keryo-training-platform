package com.yf.modules.exam.repo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.base.api.api.dto.BaseIdReqDTO;
import com.yf.base.api.api.dto.BaseIdsReqDTO;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.modules.exam.repo.dto.ImportResult;
import com.yf.modules.exam.repo.dto.RepoQuDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuDetailDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuListReqDTO;
import com.yf.modules.exam.repo.dto.request.ReviewReqDTO;
import com.yf.modules.exam.repo.service.RepoQuService;
import com.yf.modules.exam.repo.service.impl.AiQuService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 问题题目控制器
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-11 09:42
 */
@Tag(name = "试题")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exam/repo/qu")
public class RepoQuController extends BaseController {

    private final RepoQuService repoQuService;
    private final AiQuService aiQuService;

    /**
     * 添加或修改
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "添加或修改")
    @RequiresPermissions(value = {"repo:qu:add", "repo:qu:edit"}, logical = Logical.OR)
    @PostMapping("/save")
    public ApiRest<?> save(@RequestBody RepoQuDetailDTO reqDTO) {
        repoQuService.save(reqDTO);
        return super.success();
    }

    /**
     * 批量删除
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "批量删除")
    @RequiresPermissions("repo:qu:delete")
    @PostMapping("/delete")
    public ApiRest<?> delete(@RequestBody BaseIdsReqDTO reqDTO) {
        //根据ID删除
        repoQuService.delete(reqDTO.getIds());
        return super.success();
    }

    /**
     * 查找详情
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "查找详情")
    @RequiresPermissions("repo:qu:view")
    @PostMapping("/detail")
    public ApiRest<RepoQuDetailDTO> detail(@RequestBody BaseIdReqDTO reqDTO) {
        RepoQuDetailDTO dto = repoQuService.detail(reqDTO.getId());
        return super.success(dto);
    }

    /**
     * 分页查找
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "分页查找")
    @RequiresPermissions("repo:qu:view")
    @PostMapping("/paging")
    public ApiRest<IPage<RepoQuDTO>> paging(@RequestBody PagingReqDTO<RepoQuListReqDTO> reqDTO) {

        //分页查询并转换
        IPage<RepoQuDTO> page = repoQuService.paging(reqDTO);

        return super.success(page);
    }

    /**
     * 两级审核
     *
     * @param reqDTO
     * @return
     */
    @Operation(summary = "两级审核")
    @RequiresPermissions("repo:qu:edit")
    @PostMapping("/review")
    public ApiRest<?> review(@RequestBody ReviewReqDTO reqDTO) {
        repoQuService.review(reqDTO);
        return super.success();
    }

    /**
     * 一键审核（总护理长/管理员批量置 APPROVED）
     */
    @Operation(summary = "一键审核")
    @RequiresPermissions("repo:qu:edit")
    @PostMapping("/review-batch")
    public ApiRest<?> reviewBatch(@RequestBody BaseIdsReqDTO req) {
        repoQuService.batchApprove(req.getIds());
        return super.success();
    }

    /**
     * 批量导入题目（Excel xls/xlsx / Word docx 自动识别，含 XXE 防护）
     */
    @Operation(summary = "导入题目(Excel/Word)")
    @RequiresPermissions(value = {"repo:qu:add", "repo:qu:edit"}, logical = Logical.OR)
    @PostMapping("/import")
    public ApiRest<ImportResult> importFile(@RequestParam(value = "repoId", required = false) String repoId,
                                            @RequestParam("file") MultipartFile file) {
        return super.success(repoQuService.importFile(repoId, file));
    }

    /**
     * 下载 Excel 导入模板
     */
    @Operation(summary = "下载导入模板")
    @RequiresPermissions("repo:qu:view")
    @GetMapping("/import-template")
    public void importTemplate(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("题目导入模板");
            String[] cols = {"题型", "难度", "题干", "选项A", "选项B", "选项C", "选项D", "正确答案", "解析"};
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            String[][] samples = {
                    {"单选", "简单", "血液透析常用的抗凝药物是？", "肝素", "华法林", "阿司匹林", "氯吡格雷", "A", "临床常用肝素/枸橼酸抗凝"},
                    {"多选", "中等", "以下哪些属于血液透析充分性指标？", "Kt/V", "URR", "血压", "血磷", "A,B", "Kt/V与URR为常用指标"},
                    {"判断", "简单", "透析前必须测量血压。", "", "", "", "", "正确", "透析前后均需测血压"},
                    {"填空", "简单", "血液净化中常用的抗凝药物是____和____。", "", "", "", "", "肝素|枸橼酸", "两空以|分隔"},
                    {"简答", "中等", "简述血液透析的基本原理。", "", "", "", "", "通过半透膜弥散与对流清除溶质和水分", "供学员对照的参考答案"},
            };
            for (int i = 0; i < samples.length; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                for (int j = 0; j < samples[i].length; j++) {
                    row.createCell(j).setCellValue(samples[i][j]);
                }
            }
            for (int i = 0; i < cols.length; i++) {
                sheet.setColumnWidth(i, i == 2 ? 12000 : 8000);
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=question_template.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        }
    }

    /**
     * AI 生成参考答案（简答/填空）
     */
    @Operation(summary = "AI生成参考答案")
    @RequiresPermissions(value = {"repo:qu:add", "repo:qu:edit"}, logical = Logical.OR)
    @PostMapping("/ai/generate-answer")
    public ApiRest<String> aiGenerateAnswer(@RequestBody java.util.Map<String, String> req) {
        String answer = aiQuService.generateAnswer(req.get("quType"), req.get("content"));
        return super.success(answer);
    }

    /**
     * AI 出题（返回完整题目 JSON 供表单填充）
     */
    @Operation(summary = "AI出题")
    @RequiresPermissions(value = {"repo:qu:add", "repo:qu:edit"}, logical = Logical.OR)
    @PostMapping("/ai/generate-qu")
    public ApiRest<RepoQuDetailDTO> aiGenerateQu(@RequestBody java.util.Map<String, String> req) {
        RepoQuDetailDTO dto = aiQuService.generateQu(req.get("topic"), req.get("quType"), req.get("difficulty"));
        return super.success(dto);
    }

    /**
     * 根据培训(课件) AI 出题 → 入库 DRAFT → 走两级审核
     * 支持 typeCounts/typeScores 指定每种题型数量与每题分值（Map: quType->数量/分值）
     */
    @Operation(summary = "根据课件AI出题")
    @RequiresPermissions(value = {"repo:qu:add", "repo:qu:edit"}, logical = Logical.OR)
    @PostMapping("/ai/generate-from-courseware")
    public ApiRest<java.util.Map<String, Object>> aiGenerateFromCourseware(@RequestBody java.util.Map<String, Object> req) {
        // typeCounts: {"radio":5,"multi":3,"judge":2}
        java.util.Map<String, Integer> typeCounts = new java.util.HashMap<>();
        Object tc = req.get("typeCounts");
        if (tc instanceof java.util.Map<?, ?> tcMap) {
            for (java.util.Map.Entry<?, ?> e : tcMap.entrySet()) {
                try {
                    typeCounts.put(String.valueOf(e.getKey()),
                            Integer.valueOf(String.valueOf(e.getValue())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        // typeScores: {"radio":3,"multi":4,"judge":2} 每题分值
        java.util.Map<String, java.math.BigDecimal> typeScores = new java.util.HashMap<>();
        Object ts = req.get("typeScores");
        if (ts instanceof java.util.Map<?, ?> tsMap) {
            for (java.util.Map.Entry<?, ?> e : tsMap.entrySet()) {
                try {
                    typeScores.put(String.valueOf(e.getKey()),
                            new java.math.BigDecimal(String.valueOf(e.getValue())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        java.util.Map<String, Object> res = aiQuService.generateFromCourseware(
                (String) req.get("repoId"),
                (String) req.get("coursewareTitle"),
                (String) req.get("topic"),
                req.get("count") == null ? null : Integer.valueOf(req.get("count").toString()),
                (String) req.get("quTypes"),
                typeCounts, typeScores);
        return super.success(res);
    }
}

