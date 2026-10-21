package com.keryo.training.modules.trainingplan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 培训计划（T-06.3）
 * 生命周期：DRAFT →(提交)→ PENDING →(通过)→ ACTIVE →(到期)→ ENDED →(归档)→ ARCHIVED
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("el_keryo_training_plan")
public class TrainingPlan extends Model<TrainingPlan> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String title;
    private String content;
    private String examId;
    /** 关联培训内容ID（原课件，T-13b：一个培训对应一个课件） */
    private String coursewareId;
    private String deptCode;
    private String status;
    private Date startTime;
    private Date endTime;
    private String submitBy;
    private Date submitTime;
    private String approveBy;
    private Date approveTime;
    private String approveRemark;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
}

