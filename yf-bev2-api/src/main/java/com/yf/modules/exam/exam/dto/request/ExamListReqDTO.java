package com.yf.modules.exam.exam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 考试列表请求类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-23 08:29
 */
@Data
@Schema(description = "考试列表请求类")
public class ExamListReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "考试名称")
    private String title;

    @Schema(description = "时间查找L")
    private Date startTimeL;

    @Schema(description = "时间查找R")
    private Date startTimeR;

    @Schema(description = "可见考试ID集合（学员端按参与培训计划过滤，管理端忽略）")
    private List<String> examIds;


}

