package com.yf.base.utils.passwd;

import com.yf.base.utils.file.MD5Util;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.nio.charset.StandardCharsets;

/**
 * 通用的密码处理类，用于生成密码和校验密码
 * <p>
 * T-07.1 安全升级：新密码一律 bcrypt（OpenBSDBCrypt，$2a$10$ 前缀，cost=10），
 * 存量 MD5+盐密码保持兼容，登录成功时自动迁移为 bcrypt。
 */
@Log4j2
public class PassHandler {

    /** bcrypt 哈希前缀（用于识别存量 MD5 密码） */
    private static final String BCRYPT_PREFIX = "$2";

    /** bcrypt 计算强度（cost=10，安全与性能平衡） */
    private static final int BCRYPT_COST = 10;

    /**
     * checkPass:校验密码是否一致
     * 兼容两种存储：bcrypt（新）与 MD5+盐（存量）
     *
     * @param inputPass 用户传入的密码
     * @param salt      数据库保存的密码随机码（bcrypt 时为空）
     * @param pass      数据库保存的密码哈希
     * @return
     */
    public static boolean checkPass(String inputPass, String salt, String pass) {
        if (pass == null || inputPass == null) {
            return false;
        }
        // bcrypt 格式
        if (pass.startsWith(BCRYPT_PREFIX)) {
            try {
                return OpenBSDBCrypt.checkPassword(pass, inputPass.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.warn("bcrypt 校验异常", e);
                return false;
            }
        }
        // 存量 MD5+盐
        if (salt == null) {
            return false;
        }
        String pwdMd5 = MD5Util.MD5(inputPass);
        return MD5Util.MD5(pwdMd5 + salt).equals(pass);
    }

    /**
     * 是否为存量 MD5 密码（登录成功后需迁移为 bcrypt）
     */
    public static boolean isLegacyMd5(String pass) {
        return pass != null && !pass.startsWith(BCRYPT_PREFIX);
    }

    /**
     * 生成 bcrypt 密码（新用户/改密一律 bcrypt）
     */
    public static PassInfo buildPassword(String inputPass) {
        try {
            byte[] salt = new byte[16];
            new java.security.SecureRandom().nextBytes(salt);
            String hash = OpenBSDBCrypt.generate(
                    inputPass.getBytes(StandardCharsets.UTF_8), salt, BCRYPT_COST);
            return new PassInfo("", hash);
        } catch (Exception e) {
            // 兜底：bcrypt 不可用时退回 MD5+盐（不阻断注册）
            log.error("bcrypt 生成失败，回退 MD5+盐", e);
            String salt = RandomStringUtils.randomAlphabetic(6);
            String encryptPassword = MD5Util.MD5(MD5Util.MD5(inputPass) + salt);
            return new PassInfo(salt, encryptPassword);
        }
    }

    public static void main(String[] args) {
        PassInfo pa = buildPassword("4RK#nxUp1yu981Rx");
        log.info("admin password hash: {}", pa.getPassword());
        log.info("verify: {}", checkPass("4RK#nxUp1yu981Rx", "", pa.getPassword()));
    }
}

