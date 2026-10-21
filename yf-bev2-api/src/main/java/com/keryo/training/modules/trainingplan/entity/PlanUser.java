package com.keryo.training.modules.trainingplan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 培训计划-学员参与关系（T-13b）
 * 学员通过参与计划获得该计划关联的培训内容可见权
 */
@Data
@TableName("el_keryo_plan_user")
public class PlanUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String planId;
    private String userId;
    private Date createTime;
}

