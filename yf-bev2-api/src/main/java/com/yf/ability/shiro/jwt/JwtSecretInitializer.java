package com.yf.ability.shiro.jwt;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;

/** 在应用启动时加载 JWT 密钥，避免业务代码内置固定凭据。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecretInitializer {

    private final Environment environment;

    @Value("${keryo.security.jwt-secret:}")
    private String configuredSecret;

    @Value("${keryo.security.sync-secret:}")
    private String configuredSyncSecret;

    @PostConstruct
    public void initialize() {
        String secret = configuredSecret;
        if (!StringUtils.hasText(secret)) {
            if (!environment.acceptsProfiles(Profiles.of("dev"))) {
                throw new IllegalStateException(
                        "Missing keryo.security.jwt-secret; set KERYO_JWT_SECRET outside dev profile");
            }
            // 开发环境不把可复用密钥写入源码；未配置时每次启动生成临时密钥。
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            log.warn("KERYO_JWT_SECRET 未配置，dev 环境使用本次进程临时 JWT 密钥；重启后旧令牌失效");
        }
        if (secret.trim().length() < 32) {
            throw new IllegalStateException("keryo.security.jwt-secret must contain at least 32 characters");
        }
        JwtUtils.configure(secret);
        if (StringUtils.hasText(configuredSyncSecret)) {
            System.setProperty("keryo.security.sync-secret", configuredSyncSecret.trim());
        }
    }
}

