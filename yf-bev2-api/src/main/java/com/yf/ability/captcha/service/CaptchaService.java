package com.yf.ability.captcha.service;

/**
 * 验证码业务类
 *
 * @author bool
 * @date 2020-02-17 09:43
 */
public interface CaptchaService {

    /**
     * 保存验证码信息到Redis
     *
     * @param key
     * @param value
     */
    void saveCaptcha(String key, String value);

    /**
     * 校验验证码内容是否正确
     *
     * @param key
     * @param input
     * @return
     */
    boolean checkCaptcha(String key, String input);

    /**
     * 获取验证码明文（仅 dev 环境自动化测试用）
     *
     * @param key
     * @return 验证码明文，不存在返回 null
     */
    String getCaptcha(String key);
}

