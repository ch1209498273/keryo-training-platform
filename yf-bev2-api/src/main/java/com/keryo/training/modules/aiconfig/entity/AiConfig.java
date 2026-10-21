package com.keryo.training.modules.aiconfig.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * AI 服务配置（T-17：管理端可换供应商/模型/Key）
 * <p>
 * provider: deepseek/qwen/openai/kimi/zhipu/custom，均走 OpenAI 兼容 chat/completions 协议
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("el_keryo_ai_config")
public class AiConfig extends Model<AiConfig> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /** 供应商标识 */
    private String provider;
    /** 接口地址（OpenAI 兼容 chat/completions） */
    private String baseUrl;
    /** 模型名 */
    private String model;
    /** API Key */
    private String apiKey;
    /** 是否启用：0=停用/1=启用 */
    private Integer enabled;
    private Date createTime;
    private Date updateTime;
}

