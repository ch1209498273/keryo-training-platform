package com.yf.modules.exam.repo.dto.request;

import com.yf.base.api.annon.Dict;
import com.yf.modules.exam.repo.dto.RepoQuAnswerDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 问题题目数据传输类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-11 09:42
 */
@Data
@Schema(description = "问题保存&详情类")
public class RepoQuDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "ID")
    private String id;

    @Dict(dictTable = "el_repo", dicText = "title", dicCode = "id")
    @Schema(description = "所属题库")
    private String repoId;

    @Schema(description = "所属章节")
    private String chapterId;

    @Dict(dicCode = "qu_type")
    @Schema(description = "题目类型")
    private String quType;

    @Dict(dicCode = "qu_difficulty_level")
    @Schema(description = "难度等级")
    private String difficultyLevel;

    @Schema(description = "题目默认分值(AI出题时设定)")
    private BigDecimal score;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "整题解析")
    private String analysis;

    @Schema(description = "审核状态 DRAFT/REVIEW2/APPROVED/REJECTED")
    private String reviewStatus;

    @Schema(description = "当前审核层级 0未审 1一审通过 2二审")
    private Integer reviewLevel;

    @Schema(description = "一审人")
    private String firstReviewer;

    @Schema(description = "一审时间")
    private Date firstReviewTime;

    @Schema(description = "二审人")
    private String secondReviewer;

    @Schema(description = "二审时间")
    private Date secondReviewTime;

    @Schema(description = "审核意见")
    private String reviewRemark;

    @Schema(description = "选项列表")
    private List<RepoQuAnswerDTO> answerList;

}

