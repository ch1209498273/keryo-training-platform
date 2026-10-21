package com.yf.system.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.keryo.training.modules.user.mapper.ForgetPassMapper;
import com.keryo.training.modules.user.entity.ForgetPass;
import com.yf.ability.captcha.service.CaptchaService;
import com.yf.ability.redis.service.RedisService;
import com.yf.base.api.exception.ServiceException;
import com.yf.system.modules.config.service.CfgSwitchService;
import com.yf.system.modules.menu.service.SysMenuService;
import com.yf.system.modules.user.dto.request.ForgetCheckReqDTO;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.service.SysUserRoleService;
import com.keryo.training.common.security.LoginProtectionService;
import com.keryo.training.common.security.PasswordPolicyService;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ForgetPassServiceTest {

    @Test
    public void writeFailureIsReturnedAsFailure() throws Exception {
        CaptchaService captcha = mock(CaptchaService.class);
        when(captcha.checkCaptcha("captcha-key", "1234")).thenReturn(true);
        ForgetPassMapper mapper = mock(ForgetPassMapper.class);
        when(mapper.insert(any(ForgetPass.class))).thenThrow(new RuntimeException("database unavailable"));

        TestService service = new TestService(captcha);
        Field field = SysUserServiceImpl.class.getDeclaredField("forgetPassMapper");
        field.setAccessible(true);
        field.set(service, mapper);

        ForgetCheckReqDTO request = new ForgetCheckReqDTO();
        request.setUserName("nurse-1");
        request.setRealName("护士一");
        request.setCaptchaKey("captcha-key");
        request.setCaptchaValue("1234");

        try {
            service.forgetCheck(request);
        } catch (ServiceException ex) {
            assertEquals("忘记密码申请暂时无法提交，请稍后重试", ex.getMessage());
            return;
        }
        throw new AssertionError("申请写入失败不得返回成功");
    }

    private static final class TestService extends SysUserServiceImpl {
        private TestService(CaptchaService captcha) {
            super(mock(SysUserRoleService.class), mock(RedisService.class), captcha,
                    mock(CfgSwitchService.class), mock(SysMenuService.class),
                    mock(LoginProtectionService.class), mock(PasswordPolicyService.class));
        }

        @Override
        public SysUser getOne(Wrapper<SysUser> queryWrapper, boolean throwEx) {
            SysUser user = new SysUser();
            user.setId("user-1");
            user.setUserName("nurse-1");
            user.setRealName("护士一");
            return user;
        }
    }
}

