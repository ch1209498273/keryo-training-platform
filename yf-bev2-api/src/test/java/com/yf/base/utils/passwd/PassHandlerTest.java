package com.yf.base.utils.passwd;

import com.yf.base.utils.file.MD5Util;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * T-08.4 单元测试基线：密码校验（bcrypt + 存量 MD5 兼容）
 */
public class PassHandlerTest {

    @Test
    public void bcrypt_生成与校验通过() {
        PassInfo info = PassHandler.buildPassword("S3cure#Pass");
        assertTrue("应为 bcrypt 前缀", info.getPassword().startsWith("$2"));
        assertTrue(PassHandler.checkPass("S3cure#Pass", "", info.getPassword()));
    }

    @Test
    public void bcrypt_错误密码拒绝() {
        PassInfo info = PassHandler.buildPassword("S3cure#Pass");
        assertFalse(PassHandler.checkPass("wrong", "", info.getPassword()));
    }

    @Test
    public void bcrypt_每次生成盐不同() {
        PassInfo a = PassHandler.buildPassword("SamePass1");
        PassInfo b = PassHandler.buildPassword("SamePass1");
        assertNotEquals("bcrypt 随机盐应生成不同哈希", a.getPassword(), b.getPassword());
    }

    @Test
    public void 存量MD5加盐校验兼容() {
        String salt = "AbCdEf";
        String md5 = MD5Util.MD5(MD5Util.MD5("OldPass1") + salt);
        assertTrue(PassHandler.checkPass("OldPass1", salt, md5));
        assertFalse(PassHandler.checkPass("WrongPass", salt, md5));
    }

    @Test
    public void isLegacyMd5_识别存量密码() {
        assertTrue(PassHandler.isLegacyMd5(MD5Util.MD5("x")));
        assertFalse(PassHandler.isLegacyMd5("$2y$10$abc123"));
        assertFalse(PassHandler.isLegacyMd5(null));
    }

    @Test
    public void 空密码或null哈希拒绝() {
        assertFalse(PassHandler.checkPass(null, "", "$2y$10$xxx"));
        assertFalse(PassHandler.checkPass("pass", "", null));
    }
}

