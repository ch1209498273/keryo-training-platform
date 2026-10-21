package com.keryo.training.common.security;

import com.yf.ability.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import static com.keryo.training.common.constant.KeryoConstants.*;

/**
 * 登录保护——失败计数、账户锁定、Captcha 阈值
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class LoginProtectionService {

    private final RedisService redisService;

    private static final String KEY_FAIL  = "login:fail:";
    private static final String KEY_LOCK  = "login:locked:";
    private static final String KEY_IP    = "login:ip:fail:";

    /** 是否已锁定 */
    public boolean isLocked(String username) {
        return redisService.getString(KEY_LOCK + username) != null;
    }

    /** 当前失败次数 */
    public int failCount(String username) {
        String v = redisService.getString(KEY_FAIL + username);
        return v == null ? 0 : Integer.parseInt(v);
    }

    /** 是否需要验证码 */
    public boolean needCaptcha(String username) {
        return failCount(username) >= CAPTCHA_THRESHOLD;
    }

    /** 记录失败，超过阈值自动锁定 */
    public void recordFail(String username, String ip) {
        int count = failCount(username) + 1;
        redisService.set(KEY_FAIL + username, String.valueOf(count), 1800L);
        if (count >= LOGIN_FAIL_MAX) {
            redisService.set(KEY_LOCK + username, "1", LOGIN_LOCK_MINUTES * 60L);
            log.warn("用户 {} 锁定 {} 分钟", username, LOGIN_LOCK_MINUTES);
        }
        // IP 级别
        String ipCnt = redisService.getString(KEY_IP + ip);
        int ipCount = ipCnt == null ? 0 : Integer.parseInt(ipCnt);
        redisService.set(KEY_IP + ip, String.valueOf(ipCount + 1), 3600L);
    }

    public void recordIpFail(String ip) {
        String v = redisService.getString(KEY_IP + ip);
        int count = v == null ? 0 : Integer.parseInt(v);
        redisService.set(KEY_IP + ip, String.valueOf(count + 1), 3600L);
    }

    public boolean isIpBlocked(String ip) {
        String v = redisService.getString(KEY_IP + ip);
        return v != null && Integer.parseInt(v) >= IP_FAIL_MAX;
    }

    /** 登录成功后清空 */
    public void clearFail(String username) {
        redisService.del(KEY_FAIL + username);
        redisService.del(KEY_LOCK + username);
    }

    /** 锁定剩余时间描述 */
    public String lockMessage(String username) {
        return "账户已锁定，请" + LOGIN_LOCK_MINUTES + "分钟后重试";
    }
}

