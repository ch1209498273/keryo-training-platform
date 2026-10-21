package com.keryo.training.modules.aiconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keryo.training.modules.aiconfig.entity.AiConfig;

import java.util.Map;

/**
 * AI 服务配置
 */
public interface AiConfigService extends IService<AiConfig> {

    /**
     * 获取当前启用的 AI 配置（enabled=1）；无则返回 null（调用方降级到 yml 默认）
     */
    AiConfig getActiveConfig();

    /**
     * 当前实际使用的连接参数（环境变量优先，数据库值兼容回退；无 key 返回 null）
     */
    Map<String, String> effectiveEndpoint();

    /**
     * 管理端查看：返回脱敏后的配置（key 只显示尾号）
     */
    Map<String, Object> viewConfig();

    /**
     * 保存配置：key 为空字符串时保留原 key；enabled 默认 1
     */
    void saveConfig(Map<String, Object> req);

    /**
     * 测试连接：用当前配置发一条最小请求，成功返回 true；失败抛业务异常带原因
     */
    Map<String, Object> testConnection();
}

