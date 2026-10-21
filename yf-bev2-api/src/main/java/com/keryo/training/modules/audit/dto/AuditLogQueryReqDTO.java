package com.keryo.training.modules.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Read-only filters for the audit log management page.
 */
@Data
@Schema(name = "AuditLogQueryReqDTO")
public class AuditLogQueryReqDTO {

    @Schema(description = "Operator account")
    private String userName;

    @Schema(description = "Operator name")
    private String realName;

    @Schema(description = "Business module")
    private String module;

    @Schema(description = "Operation description")
    private String operation;

    @Schema(description = "SUCCESS or FAIL")
    private String result;

    @Schema(description = "Start time, format yyyy-MM-dd HH:mm:ss")
    private String createTimeStart;

    @Schema(description = "End time, format yyyy-MM-dd HH:mm:ss")
    private String createTimeEnd;
}

