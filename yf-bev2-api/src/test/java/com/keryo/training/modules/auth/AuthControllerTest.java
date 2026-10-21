package com.keryo.training.modules.auth;

import com.keryo.training.modules.auth.controller.AuthController;
import com.yf.ability.redis.service.RedisService;
import com.yf.ability.shiro.jwt.JwtUtils;
import com.yf.base.api.exception.ServiceException;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.enums.UserState;
import com.yf.system.modules.user.service.SysUserService;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthControllerTest {

    private RedisService redisService;
    private SysUserService sysUserService;
    private AuthController controller;

    @Before
    public void setUp() {
        JwtUtils.configure("unit-test-jwt-secret-which-is-long-enough-123456");
        redisService = mock(RedisService.class);
        sysUserService = mock(SysUserService.class);
        controller = new AuthController(redisService, sysUserService);
    }

    @Test
    public void refresh_requiresValidSignatureAndActiveUser() {
        SysUser user = new SysUser();
        user.setUserName("nurse-1");
        user.setState(UserState.NORMAL);
        when(redisService.getString(anyString())).thenReturn(null);
        when(sysUserService.getOne(any(), eq(false))).thenReturn(user);

        String refresh = JwtUtils.sign("nurse-1:refresh", 60);
        Map<String, String> data = controller.refresh(refresh).getData();

        assertNotNull(data.get("token"));
        assertNotNull(data.get("refreshToken"));
        assertEquals("nurse-1", JwtUtils.getUsername(data.get("token")));
    }

    @Test
    public void refresh_rejectsTamperedToken() {
        String refresh = JwtUtils.sign("nurse-1:refresh", 60);
        String tampered = refresh.substring(0, refresh.length() - 1)
                + (refresh.endsWith("a") ? "b" : "a");

        try {
            controller.refresh(tampered);
        } catch (ServiceException expected) {
            return;
        }
        throw new AssertionError("篡改签名的 refresh token 不得换发新令牌");
    }
}

