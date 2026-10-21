package com.yf.modules.exam.repo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * Excel 导入结果
 * </p>
 *
 * @author keryo
 * @since 2026-08-04
 */
@Data
@Schema(description = "Excel导入结果")
public class ImportResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总条数")
    private int total;

    @Schema(description = "成功条数")
    private int success;

    @Schema(description = "失败条数")
    private int fail;

    @Schema(description = "失败明细")
    private List<String> errors;
}

