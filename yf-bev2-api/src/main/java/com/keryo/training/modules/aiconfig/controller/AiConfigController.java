package com.keryo.training.modules.aiconfig.controller;

import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.keryo.training.modules.aiconfig.service.AiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 服务配置（T-17，仅管理员）
 */
@Tag(name = "AI 服务配置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keryo/ai-config")
public class AiConfigController extends BaseController {

    private final AiConfigService aiConfigService;

    @Operation(summary = "查看 AI 配置（key 脱敏）", description = "管理端")
    @RequiresPermissions("sys:ai-config:view")
    @PostMapping("/view")
    public ApiRest<Map<String, Object>> view() {
        return super.success(aiConfigService.viewConfig());
    }

    @Operation(summary = "保存 AI 配置", description = "仅管理员；key 留空=不修改")
    @RequiresPermissions("sys:ai-config:edit")
    @PostMapping("/save")
    public ApiRest<?> save(@RequestBody Map<String, Object> req) {
        aiConfigService.saveConfig(req);
        return super.success();
    }

    @Operation(summary = "测试 AI 连接", description = "用当前配置发一条最小请求")
    @RequiresPermissions("sys:ai-config:edit")
    @PostMapping("/test")
    public ApiRest<Map<String, Object>> test() {
        return super.success(aiConfigService.testConnection());
    }
}

