package com.yf.modules.exam.exam.dto;

import com.yf.base.api.annon.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

/**
 * <p>
 * 考试记录数据传输类
 * </p>
 *
 * @author 沉醉寒风
 * @since 2025-04-17 14:59
 */
@Data
@Schema(description = "考试记录")
public class ExamRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "ID")
    private String id;

    @Dict(dictTable = "el_sys_user", dicText = "real_name", dicCode = "id")
    @Schema(description = "用户ID")
    private String userId;

    @Dict(dictTable = "el_exam", dicText = "title", dicCode = "id")
    @Schema(description = "考试ID")
    private String examId;

    @Schema(description = "结算的试卷ID")
    private String paperId;

    @Schema(description = "考试次数")
    private Integer tryCount;

    @Schema(description = "最高分数")
    private BigDecimal maxScore;

    @Schema(description = "最近分数")
    private BigDecimal lastScore;

    @Schema(description = "是否通过")
    private Boolean passed;

    @Schema(description = "成绩公示：0=不公示/1=公示（联查，前端据此显示待公布）")
    private Integer scoreVisible;

    @Schema(description = "考生姓名（联查，导出用）")
    private String realName;

    @Schema(description = "考生科室（联查，导出用）")
    private String deptName;

    @Schema(description = "考生科室编码（联查，导出用）")
    private String deptCode;

    @Schema(description = "考试名称（联查，导出用）")
    private String examTitle;

    @Schema(description = "交卷时间（联查，导出用）")
    private Date createTime;

}

