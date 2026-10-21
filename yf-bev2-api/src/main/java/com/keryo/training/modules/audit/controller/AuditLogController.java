package com.keryo.training.modules.audit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keryo.training.modules.audit.dto.AuditLogQueryReqDTO;
import com.keryo.training.modules.audit.entity.AuditLog;
import com.keryo.training.modules.audit.service.AuditLogService;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.base.api.api.dto.PagingReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only audit log query for management users.
 */
@Tag(name = "Audit log")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/keryo/audit-log")
public class AuditLogController extends BaseController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Page audit logs")
    @RequiresPermissions("keryo:audit:view")
    @PostMapping("/paging")
    public ApiRest<IPage<AuditLog>> paging(
            @RequestBody PagingReqDTO<AuditLogQueryReqDTO> reqDTO) {
        return success(auditLogService.paging(reqDTO));
    }
}

