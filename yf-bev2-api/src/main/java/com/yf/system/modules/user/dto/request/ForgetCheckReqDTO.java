package com.yf.system.modules.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 忘记密码申请校验请求类（用户名+图形验证码，确认用户名存在后引导联系管理员重置）
 *
 * @author keryo
 * @since 2026-08-13
 */
@Data
@Schema(name = "忘记密码申请校验请求类", description = "校验用户名是否存在，用于引导联系管理员重置")
public class ForgetCheckReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "真实姓名（身份校验）")
    private String realName;

    @Schema(description = "验证码KEY")
    private String captchaKey;

    @Schema(description = "验证码值")
    private String captchaValue;
}

