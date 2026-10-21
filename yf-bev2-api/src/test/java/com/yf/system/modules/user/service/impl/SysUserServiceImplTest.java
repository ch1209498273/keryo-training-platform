package com.yf.system.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.keryo.training.common.security.LoginProtectionService;
import com.keryo.training.common.security.PasswordPolicyService;
import com.yf.ability.captcha.service.CaptchaService;
import com.yf.ability.redis.service.RedisService;
import com.yf.system.modules.config.service.CfgSwitchService;
import com.yf.system.modules.menu.service.SysMenuService;
import com.yf.system.modules.user.UserUtils;
import com.yf.system.modules.user.dto.request.SysUserLoginReqDTO;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.enums.SysUserId;
import com.yf.system.modules.user.enums.UserState;
import com.yf.system.modules.user.service.SysUserRoleService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 用户停用基线：停用必须保留用户记录，便于恢复并保留培训/考试历史。
 */
public class SysUserServiceImplTest {

    private MockedStatic<UserUtils> userUtilsMock;
    private RecordingUserService service;

    @Before
    public void setUp() {
        service = new RecordingUserService(
                mock(SysUserRoleService.class),
                mock(RedisService.class),
                mock(CaptchaService.class),
                mock(CfgSwitchService.class),
                mock(SysMenuService.class),
                mock(LoginProtectionService.class),
                mock(PasswordPolicyService.class));
        userUtilsMock = org.mockito.Mockito.mockStatic(UserUtils.class);
        userUtilsMock.when(UserUtils::getUserId).thenReturn(SysUserId.ADMIN);
        userUtilsMock.when(UserUtils::getRoleLevel).thenReturn(0);
    }

    @After
    public void tearDown() {
        userUtilsMock.close();
    }

    @Test
    public void delete_停用用户而不是物理删除() {
        service.delete(List.of("user-1"));

        assertNotNull("停用应提交状态更新", service.updatedRecord);
        assertEquals(UserState.DISABLED, service.updatedRecord.getState());
        assertFalse("用户停用不得调用物理删除", service.removeCalled);
    }

    @Test
    public void login_空密码必须拒绝而不能直接签发令牌() {
        SysUser user = new SysUser();
        user.setId("user-1");
        user.setUserName("nurse-1");
        user.setState(UserState.NORMAL);
        service.loginUser = user;
        when(service.captchaService.checkCaptcha("captcha-key", "1234")).thenReturn(true);

        SysUserLoginReqDTO req = new SysUserLoginReqDTO();
        req.setUserName("nurse-1");
        req.setPassword("   ");
        req.setCaptchaKey("captcha-key");
        req.setCaptchaValue("1234");

        try {
            service.login(req);
        } catch (com.yf.base.api.exception.ServiceException ex) {
            assertEquals("账号或密码错误！", ex.getMessage());
            return;
        }
        throw new AssertionError("空密码不得登录成功");
    }

    private static final class RecordingUserService extends SysUserServiceImpl {

        private SysUser updatedRecord;
        private boolean removeCalled;
        private SysUser loginUser;
        private final CaptchaService captchaService;

        private RecordingUserService(
                SysUserRoleService sysUserRoleService,
                RedisService redisService,
                CaptchaService captchaService,
                CfgSwitchService cfgSwitchService,
                SysMenuService sysMenuService,
                LoginProtectionService loginProtectionService,
                PasswordPolicyService passwordPolicyService) {
            super(sysUserRoleService, redisService, captchaService, cfgSwitchService, sysMenuService,
                    loginProtectionService, passwordPolicyService);
            this.captchaService = captchaService;
        }

        @Override
        public SysUser getOne(Wrapper<SysUser> queryWrapper, boolean throwEx) {
            return loginUser;
        }

        @Override
        public boolean update(SysUser entity, Wrapper<SysUser> updateWrapper) {
            updatedRecord = entity;
            return true;
        }

        @Override
        public boolean removeByIds(Collection<?> list) {
            removeCalled = true;
            return true;
        }
    }
}

