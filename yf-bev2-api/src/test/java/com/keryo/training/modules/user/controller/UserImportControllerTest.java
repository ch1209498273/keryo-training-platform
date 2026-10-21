package com.keryo.training.modules.user.controller;

import com.keryo.training.common.security.PasswordPolicyService;
import com.yf.system.modules.user.mapper.SysUserMapper;
import com.yf.system.modules.user.service.SysUserRoleService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/** 用户导入不得依赖代码内置的弱初始密码。 */
public class UserImportControllerTest {

    @Test
    public void 未配置导入初始密码时应在解析文件前失败() {
        UserImportController controller = new UserImportController(
                mock(SysUserMapper.class), mock(SysUserRoleService.class),
                mock(PasswordPolicyService.class));

        try {
            controller.batchImport(null);
            fail("未配置初始密码时必须拒绝导入");
        } catch (IllegalStateException ex) {
            assertEquals(
                    "用户导入初始密码未配置，请设置 KERYO_SECURITY_USER_IMPORT_INITIAL_PASSWORD",
                    ex.getMessage());
        }
    }
}

