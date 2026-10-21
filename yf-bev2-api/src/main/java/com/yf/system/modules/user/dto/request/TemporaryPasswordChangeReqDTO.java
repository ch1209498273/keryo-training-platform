package com.yf.system.modules.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/** One-time password change request issued after a successful login. */
@Data
@Schema(name = "TemporaryPasswordChangeReqDTO")
public class TemporaryPasswordChangeReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Short-lived one-time password change token")
    private String token;

    @Schema(description = "New password")
    private String newPassword;

    @Schema(description = "New password confirmation")
    private String confirmPassword;
}

