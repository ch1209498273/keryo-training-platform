package com.yf.modules.exam.cheat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serial;
import java.util.Date;

/**
 * <p>
 * 考试违规日志实体类（T-05 防切屏）
 * </p>
 */
@Data
@TableName("el_exam_cheat_log")
public class ExamCheatLog extends Model<ExamCheatLog> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /** 试卷ID */
    @TableField("paper_id")
    private String paperId;

    /** 考试ID */
    @TableField("exam_id")
    private String examId;

    /** 用户ID */
    @TableField("user_id")
    private String userId;

    /** 题目ID */
    @TableField("qu_id")
    private String quId;

    /** 违规类型: CUT_SCREEN切屏/IP_CHANGE换IP/FAST_ANSWER答题过快 */
    @TableField("cheat_type")
    private String cheatType;

    /** 详情 */
    private String content;

    /** IP */
    private String ip;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

}

