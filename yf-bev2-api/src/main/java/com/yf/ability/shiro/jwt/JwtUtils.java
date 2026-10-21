package com.yf.ability.shiro.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.util.StringUtils;

import java.util.Calendar;

/**
 * JWT 工具类。
 *
 * <p>签名密钥必须由启动配置注入，不能在源码中保存固定密钥。所有读取身份的
 * 入口都经过 {@link JWTVerifier}，禁止只调用 {@code JWT.decode} 信任客户端载荷。</p>
 */
public final class JwtUtils {

    private static final int EXPIRE_TIME = 24;
    private static final String SECRET_PROPERTY = "keryo.security.jwt-secret";
    private static final String SECRET_ENV = "KERYO_JWT_SECRET";
    private static volatile String configuredSecret;

    private JwtUtils() {
    }

    /** 由 Spring 启动配置调用；测试也可以显式注入隔离密钥。 */
    public static void configure(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret cannot be blank");
        }
        configuredSecret = secret.trim();
    }

    /** 验证 JWT 的签名、标准时间 claims 和 username claim。 */
    public static DecodedJWT verifyAndDecode(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("JWT token cannot be blank");
        }
        DecodedJWT jwt = verifier().verify(token);
        if (jwt.getExpiresAt() == null) {
            throw new IllegalArgumentException("JWT expiration is required");
        }
        String username = jwt.getClaim("username").asString();
        if (!StringUtils.hasText(username) || !username.equals(jwt.getSubject())) {
            throw new IllegalArgumentException("JWT username claim is required");
        }
        return jwt;
    }

    /** 校验 token 是否由当前密钥为指定用户签发。 */
    public static boolean verify(String token, String username) {
        try {
            DecodedJWT jwt = verifyAndDecode(token);
            return StringUtils.hasText(username)
                    && username.equals(jwt.getClaim("username").asString());
        } catch (Exception exception) {
            return false;
        }
    }

    /** 从经过签名校验的 Token 中取得用户名。 */
    public static String getUsername(String token) {
        return verifyAndDecode(token).getClaim("username").asString();
    }

    public static String sign(String username) {
        return sign(username, EXPIRE_TIME * 3600);
    }

    /** 生成 JWT Token 字符串（自定义秒数）。 */
    public static String sign(String username, int seconds) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("JWT username cannot be blank");
        }
        Calendar cl = Calendar.getInstance();
        cl.add(Calendar.SECOND, seconds);
        return JWT.create()
                .withSubject(username)
                .withClaim("username", username)
                .withExpiresAt(cl.getTime())
                .sign(Algorithm.HMAC256(secret()));
    }

    /** 无法验证（包括签名错误）的 Token 一律按过期/不可用处理。 */
    public static boolean expired(String token) {
        try {
            verifyAndDecode(token);
            return false;
        } catch (TokenExpiredException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private static JWTVerifier verifier() {
        return JWT.require(Algorithm.HMAC256(secret())).build();
    }

    private static String secret() {
        String secret = configuredSecret;
        if (!StringUtils.hasText(secret)) {
            secret = System.getProperty(SECRET_PROPERTY);
        }
        if (!StringUtils.hasText(secret)) {
            secret = System.getenv(SECRET_ENV);
        }
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "JWT secret is not configured; set keryo.security.jwt-secret or KERYO_JWT_SECRET");
        }
        return secret.trim();
    }
}

