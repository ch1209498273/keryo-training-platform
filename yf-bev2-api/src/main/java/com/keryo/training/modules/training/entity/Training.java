package com.keryo.training.modules.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("el_keryo_training")
public class Training extends Model<Training> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String title;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String deptCode;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;
}

