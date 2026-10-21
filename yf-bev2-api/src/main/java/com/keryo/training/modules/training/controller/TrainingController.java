package com.keryo.training.modules.training.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keryo.training.modules.training.entity.Training;
import com.keryo.training.modules.training.service.TrainingService;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.base.api.api.dto.BaseIdReqDTO;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import com.yf.system.modules.user.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import com.keryo.training.common.security.FileSecurityValidator;
import com.yf.ability.upload.factory.UploadFactory;
import com.yf.plugins.upload.local.dto.UploadRespDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

@Tag(name = "课件管理")
@RestController
@RequestMapping("/api/keryo/training")
@RequiredArgsConstructor
@RequiresAuthentication
public class TrainingController extends BaseController {

    private final TrainingService trainingService;
    private final UploadFactory uploadFactory;

    /** 学员参与 Mapper（T-13b） */
    @jakarta.annotation.Resource
    private com.keryo.training.modules.trainingplan.mapper.PlanUserMapper planUserMapper;

    /** 培训计划 Mapper（T-13b） */
    @jakarta.annotation.Resource
    private com.keryo.training.modules.trainingplan.mapper.TrainingPlanMapper trainingPlanMapper;

    @Operation(summary = "分页列表")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @PostMapping("/paging")
    public ApiRest<IPage<Training>> paging(@RequestBody PagingReqDTO<Training> req) {
        LambdaQueryWrapper<Training> q = new LambdaQueryWrapper<>();
        Training params = req.getParams();
        if (params != null) {
            if (params.getTitle() != null) q.like(Training::getTitle, params.getTitle());
            if (params.getDeptCode() != null) q.eq(Training::getDeptCode, params.getDeptCode());
            if (params.getStatus() != null) q.eq(Training::getStatus, params.getStatus());
            if (params.getFileType() != null) q.eq(Training::getFileType, params.getFileType());
        }
        q.orderByDesc(Training::getCreateTime);
        return success(trainingService.page(new Page<>(req.getCurrent(), req.getSize()), q));
    }

    /**
     * 学员端「培训」列表（T-13b）：只返回当前学员参与的 ACTIVE/ENDED 培训计划所关联的培训内容
     */
    @Operation(summary = "学员端培训列表（按参与计划过滤）")
    @PostMapping("/my")
    public ApiRest<java.util.List<java.util.Map<String, Object>>> myList() {
        String userId = UserUtils.getUserId();
        java.util.List<com.keryo.training.modules.trainingplan.entity.PlanUser> pus =
                planUserMapper.selectList(new LambdaQueryWrapper<com.keryo.training.modules.trainingplan.entity.PlanUser>()
                        .eq(com.keryo.training.modules.trainingplan.entity.PlanUser::getUserId, userId));
        if (pus.isEmpty()) {
            return success(java.util.List.of());
        }
        java.util.List<String> planIds = pus.stream()
                .map(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId)
                .toList();
        java.util.List<com.keryo.training.modules.trainingplan.entity.TrainingPlan> plans =
                trainingPlanMapper.selectList(new LambdaQueryWrapper<com.keryo.training.modules.trainingplan.entity.TrainingPlan>()
                        .in(com.keryo.training.modules.trainingplan.entity.TrainingPlan::getId, planIds)
                        .in(com.keryo.training.modules.trainingplan.entity.TrainingPlan::getStatus, "ACTIVE", "ENDED"));
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.keryo.training.modules.trainingplan.entity.TrainingPlan plan : plans) {
            if (plan.getCoursewareId() == null) {
                continue;
            }
            Training tr = trainingService.getById(plan.getCoursewareId());
            if (tr == null) {
                continue;
            }
            java.util.Map<String, Object> m = new HashMap<>();
            m.put("id", tr.getId());
            m.put("title", tr.getTitle());
            m.put("fileUrl", tr.getFileUrl());
            m.put("fileType", tr.getFileType());
            m.put("fileSize", tr.getFileSize());
            m.put("planTitle", plan.getTitle());
            m.put("planId", plan.getId());
            m.put("examId", plan.getExamId());
            result.add(m);
        }
        return success(result);
    }

    @Operation(summary = "创建课件")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiRest<Training> create(@RequestBody Training cw) {
        cw.setCreateTime(new Date());
        cw.setUpdateTime(new Date());
        if (cw.getStatus() == null) cw.setStatus(0);
        trainingService.save(cw);
        return success(cw);
    }

    @Operation(summary = "更新课件")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @PutMapping("/{id}")
    public ApiRest<Training> update(@PathVariable String id, @RequestBody Training cw) {
        cw.setId(id);
        cw.setUpdateTime(new Date());
        trainingService.updateById(cw);
        return success(cw);
    }

    @Operation(summary = "删除课件")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @DeleteMapping("/{id}")
    public ApiRest<?> delete(@PathVariable String id) {
        trainingService.removeById(id);
        return success();
    }

    @Operation(summary = "课件详情")
    @PostMapping("/detail")
    public ApiRest<Training> detail(@RequestBody BaseIdReqDTO dto) {
        assertReadable(dto.getId());
        Training training = trainingService.getById(dto.getId());
        if (training == null) {
            throw new ServiceException("培训资料不存在");
        }
        return success(training);
    }

    // ===== T-03.1: 安全上传 =====
    @Operation(summary = "上传课件文件")
    @RequiresRoles(value = {"admin", "keryo_director", "keryo_assistant"}, logical = Logical.OR)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiRest<Training> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "status", required = false) Integer status) {
        FileSecurityValidator.validate(file);
        String safeName = FileSecurityValidator.sanitize(file.getOriginalFilename());
        UploadRespDTO uploadResp = uploadFactory.getService().upload(file);

        Training cw = new Training();
        cw.setTitle(title != null ? title : safeName);
        cw.setFileUrl(uploadResp.getUrl());
        cw.setFileType(safeName.substring(safeName.lastIndexOf('.') + 1).toUpperCase());
        cw.setFileSize(file.getSize());
        cw.setStatus(status != null ? status : 0);
        cw.setCreateTime(new Date());
        cw.setUpdateTime(new Date());
        trainingService.save(cw);
        return success(cw);
    }

    // ===== T-03.2: 预览 =====
    @Operation(summary = "获取预览URL")
    @PostMapping("/{id}/preview")
    public ApiRest<Map<String, String>> preview(@PathVariable String id) {
        assertReadable(id);
        Training cw = trainingService.getById(id);
        if (cw == null) {
            throw new ServiceException("培训资料不存在");
        }
        Map<String, String> result = new HashMap<>();
        String ft = cw.getFileType() != null ? cw.getFileType().toUpperCase() : "";

        if ("PDF".equals(ft)) {
            result.put("type", "pdf");
            result.put("url", cw.getFileUrl());
        } else if (Set.of("PPTX", "PPT", "DOCX", "DOC").contains(ft)) {
            String encoded = URLEncoder.encode(cw.getFileUrl(), StandardCharsets.UTF_8);
            result.put("type", "office");
            result.put("url", "https://view.officeapps.live.com/op/embed.aspx?src=" + encoded);
        } else {
            result.put("type", "download");
            result.put("url", cw.getFileUrl());
        }
        return success(result);
    }

    // ===== T-03.3: 文件下载（鉴权） =====
    @Operation(summary = "下载课件文件")
    @GetMapping("/{id}/file")
    public void download(@PathVariable String id, HttpServletResponse response) throws IOException {
        assertReadable(id);
        Training cw = trainingService.getById(id);
        if (cw == null) {
            response.sendError(404, "培训资料不存在");
            return;
        }
        String url = cw.getFileUrl();
        // 从 URL 提取本地路径（/upload/file/xxx → 本地文件）
        String localPath = url;
        if (url.contains("/upload/file/")) {
            localPath = url.substring(url.indexOf("/upload/file/"));
        }
        java.io.File file = new java.io.File(localPath);
        if (!file.exists()) {
            response.sendError(404, "文件不存在");
            return;
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" +
            URLEncoder.encode(cw.getTitle(), StandardCharsets.UTF_8) + "." +
            (cw.getFileType() != null ? cw.getFileType().toLowerCase() : "bin") + "\"");
        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
        }
    }

    /**
     * 管理端可查看全部培训资料；学员端只能读取其 ACTIVE/ENDED 计划关联的课件。
     * 列表隐藏不是授权边界，详情、预览和下载也必须重复校验。
     */
    private void assertReadable(String trainingId) {
        if (trainingId == null || trainingId.isBlank()) {
            throw new ServiceException("培训资料 ID 不能为空");
        }
        if (isTrainingManager()) {
            return;
        }
        String userId = UserUtils.getUserId();
        List<com.keryo.training.modules.trainingplan.entity.PlanUser> assignments =
                planUserMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                        com.keryo.training.modules.trainingplan.entity.PlanUser>()
                        .eq("user_id", userId));
        List<String> planIds = assignments.stream()
                .map(com.keryo.training.modules.trainingplan.entity.PlanUser::getPlanId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (planIds.isEmpty()) {
            throw new ServiceException("无权访问该培训资料");
        }
        Long count = trainingPlanMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                com.keryo.training.modules.trainingplan.entity.TrainingPlan>()
                .eq("courseware_id", trainingId)
                .in("id", planIds)
                .in("status", List.of("ACTIVE", "ENDED")));
        if (count == null || count == 0) {
            throw new ServiceException("无权访问该培训资料");
        }
    }

    private boolean isTrainingManager() {
        List<String> roles = UserUtils.getRoles();
        return roles != null && (roles.contains("admin") || roles.contains("keryo_director")
                || roles.contains("keryo_assistant"));
    }
}

