package com.yf.ability.captcha.service.impl;


import com.yf.ability.captcha.service.CaptchaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码业务类
 * <p>
 * 存储改为本地内存缓存（验证码为短生命周期数据，单机部署内存足够）：
 * 不再依赖 Redis，避免 Redis 不可用时"验证码不正确或已失效"
 *
 * @author bool
 * @date 2020-02-21 10:05
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * 验证码缓存前缀
     */
    private static final String CAPTCHA_PREFIX = "sys:captcha:";

    /**
     * 验证码有效期（秒）
     */
    private static final long EXPIRE_SECONDS = 300L;

    /**
     * 本地内存缓存：key -> 验证码条目
     */
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    private static class Entry {
        final String value;
        final long expireAt;

        Entry(String value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }

    @Override
    public void saveCaptcha(String key, String value) {
        cache.put(appendKey(key), new Entry(value, System.currentTimeMillis() + EXPIRE_SECONDS * 1000));
    }

    @Override
    public boolean checkCaptcha(String key, String input) {

        // 完整KEY
        String fullKey = appendKey(key);

        Entry entry = cache.get(fullKey);
        // 不存在或已过期
        if (entry == null || entry.expireAt < System.currentTimeMillis()) {
            cache.remove(fullKey);
            return false;
        }

        // 校验
        boolean result = StringUtils.isNotBlank(entry.value) && entry.value.equalsIgnoreCase(input);

        // 验证正确就清除
        if (result) {
            cache.remove(fullKey);
        }

        return result;
    }

    @Override
    public String getCaptcha(String key) {
        Entry entry = cache.get(appendKey(key));
        if (entry == null || entry.expireAt < System.currentTimeMillis()) {
            return null;
        }
        return entry.value;
    }

    /**
     * 组合KEY
     *
     * @param key
     * @return
     */
    private String appendKey(String key) {
        return CAPTCHA_PREFIX + key;
    }
}

