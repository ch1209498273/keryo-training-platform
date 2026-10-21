package com.yf.system.modules.user.utils;

import com.yf.base.utils.Md5Util;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


/**
 * MD5签名验证工具
 *
 * @author bool
 */
@Log4j2
public class SignUtils {

    private static final String SECRET_PROPERTY = "keryo.security.sync-secret";
    private static final String SECRET_ENV = "KERYO_SYNC_SECRET";

    /**
     * 校验token是否正确
     *
     * @param userName
     * @param timestamp
     * @param sign
     * @return
     */
    public static boolean checkToken(String userName, Long timestamp, String sign) {

        if (timestamp == null || Math.abs(System.currentTimeMillis() / 1000 - timestamp) > 300) {
            return false;
        }

        // 拼字符
        String str = String.format("%s-%s-%s", userName, timestamp, secret());

        // 进行第二次MD5
        String md5 = Md5Util.md5(str);

        return sign != null && MessageDigest.isEqual(md5.getBytes(StandardCharsets.US_ASCII),
                sign.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * 生成Token
     *
     * @param userName
     * @param timestamp
     * @return
     */
    public static String generateToken(String userName, Long timestamp) {

        // 拼接字符串
        String str = String.format("%s-%s-%s", userName, timestamp, secret());

        // 直接MD5
        String sign = Md5Util.md5(str);

        return sign;
    }


    /**
     * 构建跳转URL
     *
     * @param host
     * @param userName
     * @param realName
     * @param role
     * @return
     */
    public static String generateUrl(String host, String userName, String realName, String role, String departs) {

        // 获得当前timestamp
        Long timestamp = System.currentTimeMillis() / 1000;
        String token = generateToken(userName, timestamp);
        // 构建URL
        String url = String.format("%s/api/sys/user/sync-login?userName=%s&realName=%s&timestamp=%s&departs=%s&role=%s&sign=%s",
                host,
                userName,
                realName,
                timestamp,
                departs,
                role,
                token);

        return url;
    }

    private static String secret() {
        String secret = System.getProperty(SECRET_PROPERTY);
        if (secret == null || secret.isBlank()) {
            secret = System.getenv(SECRET_ENV);
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Sync-login secret is not configured; set keryo.security.sync-secret or KERYO_SYNC_SECRET");
        }
        return secret.trim();
    }

    public static void main(String[] args) {
        // 构建跳转地址
        String url = generateUrl("http://120.211.98.242:18088", "zhangsan202204143", "可能乱码04143", "student", "北京云帆,技术部");
        log.info("跳转地址：" + url);
    }
}

