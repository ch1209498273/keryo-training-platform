package com.keryo.training.common.security;

import java.util.regex.Pattern;

/**
 * XSS 清洗工具（T-07.4）
 * 白名单标签过滤：仅保留基础排版标签，移除 script/iframe/on* 事件等危险内容
 */
public class XssSanitizeUtil {

    private XssSanitizeUtil() {
    }

    /** 危险标签（整块移除） */
    private static final Pattern DANGEROUS_TAGS = Pattern.compile(
            "<\\s*(script|iframe|object|embed|link|meta|style|form|input|button|svg|math)\\b[^>]*>.*?<\\s*/\\s*\\1\\s*>|<\\s*(script|iframe|object|embed|link|meta|style|form|input|button|svg|math)\\b[^>]*/?>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 事件属性（onclick/onerror 等） */
    private static final Pattern EVENT_ATTRS = Pattern.compile(
            "\\son[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]*)",
            Pattern.CASE_INSENSITIVE);

    /** javascript: 伪协议 */
    private static final Pattern JS_PROTOCOL = Pattern.compile(
            "(href|src|action|data)\\s*=\\s*['\"]?\\s*javascript\\s*:",
            Pattern.CASE_INSENSITIVE);

    /** 白名单标签（含属性保留） */
    private static final Pattern WHITELIST_TAG = Pattern.compile(
            "<\\s*/?\\s*(p|br|strong|b|em|i|u|ul|ol|li|h[1-6]|span|div|table|thead|tbody|tr|td|th|blockquote|pre|code|hr|img|a)\\b[^>]*>",
            Pattern.CASE_INSENSITIVE);

    /**
     * 清洗 HTML：移除危险标签/事件属性/javascript 协议，保留白名单标签
     */
    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        String out = html;
        // 1. 整块移除危险标签
        out = DANGEROUS_TAGS.matcher(out).replaceAll("");
        // 2. 移除事件属性
        out = EVENT_ATTRS.matcher(out).replaceAll("");
        // 3. 移除 javascript: 协议
        out = JS_PROTOCOL.matcher(out).replaceAll("$1=\"\"");
        // 4. 非白名单标签转义为纯文本（保留内容）
        // 用占位替换保护白名单标签，剩余标签转义
        java.util.List<String> keep = new java.util.ArrayList<>();
        out = WHITELIST_TAG.matcher(out).replaceAll(m -> {
            keep.add(m.group());
            return "\u0000" + (keep.size() - 1) + "\u0000";
        });
        out = out.replaceAll("<[^>]*>", "");
        for (int i = 0; i < keep.size(); i++) {
            out = out.replace("\u0000" + i + "\u0000", keep.get(i));
        }
        return out;
    }
}

