package com.keryo.training.modules.aiconfig.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.keryo.training.modules.aiconfig.entity.AiConfig;
import com.keryo.training.modules.aiconfig.mapper.AiConfigMapper;
import com.keryo.training.modules.aiconfig.service.AiConfigService;
import com.yf.base.api.exception.ServiceException;
import com.yf.base.utils.jackson.JsonHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 服务配置（T-17）
 */
@Log4j2
@Service
public class AiConfigServiceImpl extends ServiceImpl<AiConfigMapper, AiConfig> implements AiConfigService {

    /** 默认配置（数据库无启用配置时的兜底） */
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-v4-flash";
    private static final String LEGACY_BASE_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String LEGACY_MODEL = "deepseek-chat";

    @jakarta.annotation.Resource
    private org.springframework.core.env.Environment environment;

    /** key 脱敏：sk-***f0e9（保留前6后4） */
    private static String maskKey(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        if (key.length() <= 10) {
            return "****";
        }
        return key.substring(0, 6) + "***" + key.substring(key.length() - 4);
    }

    @Override
    public AiConfig getActiveConfig() {
        QueryWrapper<AiConfig> w = new QueryWrapper<>();
        w.lambda().eq(AiConfig::getEnabled, 1).last("LIMIT 1");
        return this.getOne(w, false);
    }

    /** Current connection parameters; provider-specific environment secrets take precedence over DB. */
    public Map<String, String> effectiveEndpoint() {
        AiConfig cfg = getActiveConfig();
        String baseUrl = effectiveBaseUrl(cfg);
        String model = effectiveModel(cfg);
        String apiKey = configuredApiKey(cfg);
        if (isNotBlank(apiKey)) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("baseUrl", baseUrl);
            m.put("model", model);
            m.put("apiKey", apiKey);
            return m;
        }
        return null;
    }

    @Override
    public Map<String, Object> viewConfig() {
        AiConfig cfg = getActiveConfig();
        Map<String, Object> m = new LinkedHashMap<>();
        if (cfg != null) {
            m.put("id", cfg.getId());
            m.put("provider", cfg.getProvider());
            m.put("baseUrl", effectiveBaseUrl(cfg));
            m.put("model", effectiveModel(cfg));
            String apiKey = configuredApiKey(cfg);
            m.put("apiKeyMasked", maskKey(apiKey));
            m.put("hasKey", isNotBlank(apiKey));
            m.put("enabled", cfg.getEnabled() == null ? 1 : cfg.getEnabled());
        } else {
            m.put("provider", "deepseek");
            m.put("baseUrl", DEFAULT_BASE_URL);
            m.put("model", DEFAULT_MODEL);
            String apiKey = environmentApiKey("deepseek");
            m.put("apiKeyMasked", maskKey(apiKey));
            m.put("hasKey", isNotBlank(apiKey));
            m.put("enabled", 1);
        }
        return m;
    }

    @Override
    public void saveConfig(Map<String, Object> req) {
        String id = (String) req.getOrDefault("id", "");
        String provider = str(req.get("provider"));
        String baseUrl = str(req.get("baseUrl"));
        String model = str(req.get("model"));
        String apiKey = str(req.get("apiKey"));
        Object enabledObj = req.get("enabled");

        if (!isNotBlank(baseUrl)) {
            throw new ServiceException("接口地址不能为空");
        }
        if (!isNotBlank(model)) {
            throw new ServiceException("模型名不能为空");
        }

        AiConfig cfg = isNotBlank(id) ? this.getById(id) : null;
        if (cfg == null) {
            cfg = new AiConfig();
            cfg.setProvider(isNotBlank(provider) ? provider : "custom");
        } else {
            if (isNotBlank(provider)) {
                cfg.setProvider(provider);
            }
        }
        cfg.setBaseUrl(baseUrl.trim());
        cfg.setModel(model.trim());
        // key 为空字符串 = 不修改（保留原 key）
        if (isNotBlank(apiKey)) {
            cfg.setApiKey(apiKey.trim());
        }
        cfg.setEnabled(enabledObj == null ? 1 : Integer.valueOf(String.valueOf(enabledObj)));
        this.saveOrUpdate(cfg);
        log.info("AI 配置已保存 provider={} model={}", cfg.getProvider(), cfg.getModel());
    }

    @Override
    public Map<String, Object> testConnection() {
        Map<String, String> ep = effectiveEndpoint();
        if (ep == null) {
            throw new ServiceException("AI 服务未配置 API Key，请先在 AI 设置里填写");
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", ep.get("model"));
            body.put("messages", List.of(
                    Map.of("role", "user", "content", "你好，请回复：OK")));
            body.put("max_tokens", 8);
            body.put("temperature", 0);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ep.get("baseUrl")))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + ep.get("apiKey"))
                    .POST(HttpRequest.BodyPublishers.ofString(JsonHelper.toJson(body), java.nio.charset.StandardCharsets.UTF_8))
                    .build();
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                JsonNode root = JsonHelper.getMapper().readTree(resp.body());
                String msg = root.path("error").path("message").asText(resp.body());
                throw new ServiceException("连接失败(HTTP " + resp.statusCode() + ")：" + abbrev(msg, 120));
            }
            JsonNode root = JsonHelper.getMapper().readTree(resp.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ok", true);
            m.put("reply", abbrev(content, 50));
            return m;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 测试连接失败", e);
            throw new ServiceException("连接失败：" + abbrev(e.getMessage(), 120));
        }
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Existing databases may still contain the old DeepSeek pair. Normalize only that
     * exact legacy default; custom provider settings remain untouched.
     */
    private static String effectiveBaseUrl(AiConfig cfg) {
        if (cfg == null || !isNotBlank(cfg.getBaseUrl())) {
            return DEFAULT_BASE_URL;
        }
        if (isLegacyDeepSeekDefault(cfg)) {
            return DEFAULT_BASE_URL;
        }
        return cfg.getBaseUrl();
    }

    private static String effectiveModel(AiConfig cfg) {
        if (cfg == null || !isNotBlank(cfg.getModel())) {
            return DEFAULT_MODEL;
        }
        if (isLegacyDeepSeekDefault(cfg)) {
            return DEFAULT_MODEL;
        }
        return cfg.getModel();
    }

    private static boolean isLegacyDeepSeekDefault(AiConfig cfg) {
        return "deepseek".equalsIgnoreCase(cfg.getProvider())
                && LEGACY_BASE_URL.equalsIgnoreCase(cfg.getBaseUrl().trim())
                && LEGACY_MODEL.equalsIgnoreCase(cfg.getModel().trim());
    }

    /** Environment secrets take precedence over the legacy database value. */
    private String configuredApiKey(AiConfig cfg) {
        String provider = cfg == null ? "deepseek" : cfg.getProvider();
        String environmentKey = environmentApiKey(provider);
        if (isNotBlank(environmentKey)) {
            return environmentKey;
        }
        return cfg == null ? "" : cfg.getApiKey();
    }

    private String environmentApiKey(String provider) {
        if (environment == null) {
            return "";
        }
        String normalizedProvider = provider == null ? "" : provider
                .toUpperCase(java.util.Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "_");
        String providerKey = isNotBlank(normalizedProvider)
                ? environment.getProperty("AI_" + normalizedProvider + "_API_KEY", "")
                : "";
        if (isNotBlank(providerKey)) {
            return providerKey.trim();
        }
        return environment.getProperty("AI_API_KEY", "").trim();
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String abbrev(String s, int n) {
        if (s == null) {
            return "";
        }
        return s.length() > n ? s.substring(0, n) + "…" : s;
    }
}

