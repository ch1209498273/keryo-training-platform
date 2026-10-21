package com.keryo.training.common.constant;

/**
 * 医护培训平台常量
 */
public interface KeryoConstants {

    // -- 角色 ID --
    String ROLE_DIRECTOR   = "keryo_director";
    String ROLE_SUPERVISOR = "keryo_supervisor";
    String ROLE_HEAD_NURSE = "keryo_head_nurse";
    String ROLE_NURSE      = "keryo_nurse";
    String ROLE_ASSISTANT  = "keryo_assistant";

    // -- 登录保护 --
    int LOGIN_FAIL_MAX       = 5;
    int LOGIN_LOCK_MINUTES   = 15;
    int CAPTCHA_THRESHOLD    = 3;
    int IP_FAIL_MAX          = 20;
    int IP_LOCK_MINUTES      = 30;

    // -- 密码策略 --
    int PASSWORD_HISTORY_SIZE  = 5;
    int PASSWORD_EXPIRE_DAYS   = 90;
    int PASSWORD_WARN_DAYS     = 7;
    int TEMP_PASSWORD_TOKEN_SECONDS = 10 * 60;

    // -- Token --
    int ACCESS_TOKEN_EXPIRE_SECONDS  = 15 * 60;     // 15min
    int REFRESH_TOKEN_EXPIRE_SECONDS = 7 * 24 * 3600; // 7 days
}

