package com.keryo.training.modules.auth.controller;

import com.yf.ability.shiro.jwt.JwtUtils;
import com.yf.ability.redis.service.RedisService;
import com.yf.base.api.api.ApiError;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.exception.ServiceException;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.enums.UserState;
import com.yf.system.modules.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证补充接口——Token 刷新
 */
@Log4j2
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class AuthController {

    private final RedisService redisService;
    private final SysUserService sysUserService;

    private static final String BLACKLIST_PREFIX = "refresh:blacklist:";

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public ApiRest<Map<String, String>> refresh(
            @RequestHeader(value = "token", required = false) String oldRefreshToken) {
        if (StringUtils.isBlank(oldRefreshToken)) {
            throw new ServiceException("刷新令牌不能为空");
        }
        // 检查黑名单
        if (redisService.getString(BLACKLIST_PREFIX + oldRefreshToken) != null) {
            throw new ServiceException("刷新令牌已被使用，请重新登录");
        }
        try {
            String username = JwtUtils.getUsername(oldRefreshToken);
            if (!username.endsWith(":refresh")) {
                throw new ServiceException("无效的刷新令牌");
            }
            String realUsername = username.substring(0, username.length() - ":refresh".length());

            SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>()
                    .select("id", "user_name", "state")
                    .eq("user_name", realUsername), false);
            if (user == null || !UserState.NORMAL.equals(user.getState())) {
                throw new ServiceException("账号当前不可刷新登录令牌");
            }

            // 颁发新双 token
            Map<String, String> result = new HashMap<>();
            result.put("token", JwtUtils.sign(realUsername, 15 * 60));
            result.put("refreshToken", JwtUtils.sign(realUsername + ":refresh", 7 * 24 * 3600));
            // 只有新令牌签发成功后才撤销旧令牌，避免配置错误造成无故登出。
            redisService.set(BLACKLIST_PREFIX + oldRefreshToken, "1", 7 * 24 * 3600L);
            ApiRest<Map<String, String>> resp = new ApiRest<>();
            resp.setCode(0);
            resp.setMsg("刷新成功");
            resp.setData(result);
            return resp;
        } catch (TokenExpiredException e) {
            throw new ServiceException(ApiError.ERROR_90010004);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            throw new ServiceException("Token刷新失败，请重新登录");
        }
    }
}

