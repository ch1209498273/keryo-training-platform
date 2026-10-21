package com.keryo.training.modules.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 审计日志（T-07.2）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("el_keryo_audit_log")
public class AuditLog extends Model<AuditLog> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String userId;
    private String userName;
    private String realName;
    private String module;
    private String operation;
    private String method;
    private String url;
    private String ip;
    private String params;
    private String result;
    private String errorMsg;
    private Long costTime;
    private Date createTime;
}

