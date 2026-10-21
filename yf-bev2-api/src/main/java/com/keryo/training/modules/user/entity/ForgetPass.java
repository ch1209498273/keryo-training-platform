package com.keryo.training.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 忘记密码申请记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("el_keryo_forget_pass")
public class ForgetPass extends Model<ForgetPass> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String userId;
    private String userName;
    private String realName;
    /** 0待处理 1已处理 */
    private Integer status;
    private String remark;
    private String handleBy;
    private Date handleTime;
    private Date createTime;
}

