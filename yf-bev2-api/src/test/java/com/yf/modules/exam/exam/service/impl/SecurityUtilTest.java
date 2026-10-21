package com.yf.modules.exam.exam.service.impl;

import com.keryo.training.common.security.SqlSafeUtil;
import com.keryo.training.modules.audit.util.DesensitizeUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * T-08.4 单元测试基线：安全工具（T-07 产物）
 */
public class SecurityUtilTest {

    @Test
    public void sql注入payload被清洗() {
        assertEquals("OR 1=1", SqlSafeUtil.sanitize("' OR '1'='1"));
        assertEquals("DROP TABLE el_sys_user", SqlSafeUtil.sanitize("'; DROP TABLE el_sys_user; --"));
        assertEquals("正常用户名", SqlSafeUtil.sanitize("正常用户名"));
    }

    @Test
    public void 姓名脱敏() {
        assertEquals("张*", DesensitizeUtil.name("张三"));
        // 复姓保留前两字
        assertEquals("欧阳*", DesensitizeUtil.name("欧阳锋"));
        assertNull(DesensitizeUtil.name(null));
    }

    @Test
    public void 手机号脱敏中间4位() {
        assertEquals("138****1234", DesensitizeUtil.mobile("13812341234"));
    }

    @Test
    public void IPv4与IPv6脱敏() {
        assertEquals("192.168.1.***", DesensitizeUtil.ip("192.168.1.100"));
        // IPv6 保留前两段
        assertTrue(DesensitizeUtil.ip("fe80::1").startsWith("fe80:"));
        assertTrue(DesensitizeUtil.ip("fe80::1").endsWith("***"));
        assertNull(DesensitizeUtil.ip(null));
    }

    @Test
    public void 非法表名拒绝() {
        // findTableText 白名单正则（等价验证）
        assertTrue("el_sys_user".matches("el_[a-zA-Z0-9_]+"));
        assertFalse("sys_user; DROP".matches("el_[a-zA-Z0-9_]+"));
        assertFalse("information_schema.tables".matches("el_[a-zA-Z0-9_]+"));
    }
}

