package com.keryo.training.modules.retention.controller;

import com.keryo.training.modules.audit.annotation.AuditLog;
import com.keryo.training.modules.retention.service.RetentionService;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Manual, non-destructive retention archive entry point. */
@Tag(name = "Retention archive")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keryo/retention")
public class RetentionController extends BaseController {

    private final RetentionService retentionService;

    @Operation(summary = "Preview rows eligible for archive")
    @RequiresPermissions("keryo:audit:view")
    @PostMapping("/preview")
    public ApiRest<Map<String, Long>> preview() {
        return success(retentionService.preview());
    }

    @Operation(summary = "Archive eligible rows without deleting source rows")
    @AuditLog(module = "数据保留", operation = "执行非破坏归档")
    @RequiresPermissions("keryo:audit:view")
    @PostMapping("/archive")
    public ApiRest<Map<String, Long>> archive() {
        return success(retentionService.archive());
    }
}

