package com.yf.system.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/** Stored password hashes used to enforce the password reuse window. */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("el_sys_user_password_history")
public class SysUserPasswordHistory extends Model<SysUserPasswordHistory> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("user_id")
    private String userId;

    private String password;

    @TableField("create_time")
    private Date createTime;
}

