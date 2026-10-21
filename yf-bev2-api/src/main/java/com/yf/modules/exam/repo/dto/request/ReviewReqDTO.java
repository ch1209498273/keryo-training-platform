package com.yf.modules.exam.repo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 题目两级审核请求
 * </p>
 *
 * @author keryo
 * @since 2026-08-04
 */
@Data
@Schema(description = "题目审核请求")
public class ReviewReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    private String quId;

    @Schema(description = "审核层级：1=一审，2=二审")
    private Integer level;

    @Schema(description = "审核动作：APPROVE=通过，REJECT=驳回，RESUBMIT=重新提交")
    private String action;

    @Schema(description = "审核意见")
    private String remark;
}

