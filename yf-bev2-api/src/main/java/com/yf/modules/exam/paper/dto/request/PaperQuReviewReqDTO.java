package com.yf.modules.exam.paper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * ESSAY 主观题人工阅卷请求类（T-05）
 * </p>
 */
@Data
@Schema(description = "ESSAY 主观题人工阅卷请求类")
public class PaperQuReviewReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "试卷ID")
    private String paperId;

    @Schema(description = "试题ID")
    private String quId;

    @Schema(description = "阅卷得分（0~满分）")
    private BigDecimal actualScore;
}

