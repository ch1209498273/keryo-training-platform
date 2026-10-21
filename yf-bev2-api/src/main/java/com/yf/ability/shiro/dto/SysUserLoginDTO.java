package com.yf.ability.shiro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yf.base.api.annon.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 管理用户请求类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2020-04-13 16:57
 */
@Data
@Schema(name = "管理用户登录响应类", description = "管理用户登录响应类")
public class SysUserLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "ID")
    private String id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机")
    private String mobile;

    @Schema(description = "数据权限")
    private Integer dataScope;

    @Schema(description = "权限级别")
    private Integer roleLevel;

    @Dict(dictTable = "el_sys_depart", dicCode = "dept_code", dicText = "dept_name")
    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "管辖区域(督导,权限v2)")
    private String areaCode;

    @Schema(description = "状态")
    private Integer state;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "登录令牌")
    private String token;

    @Schema(description = "刷新令牌（7天有效）")
    @JsonProperty("refreshToken")
    private String refreshToken;

    /** Whether the login may only continue through the temporary password flow. */
    private boolean mustChangePassword;

    /** One-time token for the independent temporary password change endpoint. */
    private String passwordChangeToken;

    /** Remaining days when the password is within the warning window. */
    private Integer passwordExpireWarningDays;
}

