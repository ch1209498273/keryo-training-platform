package com.yf.modules.exam.repo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 问题题目实体类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-11 09:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("el_repo_qu")
public class RepoQu extends Model<RepoQu> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 所属题库
     */
    @TableField("repo_id")
    private String repoId;

    /**
     * 所属章节
     */
    @TableField("chapter_id")
    private String chapterId;

    /**
     * 题目类型
     */
    @TableField("qu_type")
    private String quType;

    /**
     * 难度等级
     */
    @TableField("difficulty_level")
    private String difficultyLevel;

    /**
     * 题目默认分值(AI出题时设定,建考试自动带出)
     */
    private BigDecimal score;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 整题解析
     */
    private String analysis;

    /**
     * 审核状态：DRAFT=草稿(待一审)，REVIEW2=一审通过待二审，APPROVED=已通过，REJECTED=已驳回
     */
    @TableField("review_status")
    private String reviewStatus;

    /**
     * 当前审核层级：0=未审，1=一审通过，2=二审
     */
    @TableField("review_level")
    private Integer reviewLevel;

    /**
     * 一审人
     */
    @TableField("first_reviewer")
    private String firstReviewer;

    /**
     * 一审时间
     */
    @TableField("first_review_time")
    private Date firstReviewTime;

    /**
     * 二审人
     */
    @TableField("second_reviewer")
    private String secondReviewer;

    /**
     * 二审时间
     */
    @TableField("second_review_time")
    private Date secondReviewTime;

    /**
     * 审核意见
     */
    @TableField("review_remark")
    private String reviewRemark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 修改人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

}

