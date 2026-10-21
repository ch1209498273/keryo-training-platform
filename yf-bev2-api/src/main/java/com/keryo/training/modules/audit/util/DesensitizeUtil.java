package com.keryo.training.modules.audit.util;

/**
 * PII 脱敏工具（T-07.2）
 */
public class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 姓名脱敏：单姓留首字（张*），复姓留前两字（欧阳*）
     */
    public static String name(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        String trimmed = name.trim();
        // 常见复姓（欧阳/司马/上官/诸葛/西门/东方/独孤/慕容/皇甫/长孙）
        String[] compound = {"欧阳", "司马", "上官", "诸葛", "西门", "东方", "独孤", "慕容", "皇甫", "长孙"};
        for (String c : compound) {
            if (trimmed.startsWith(c)) {
                return c + "*";
            }
        }
        return trimmed.charAt(0) + "*";
    }

    /**
     * 手机号脱敏：中间4位打码，如 138****1234
     */
    public static String mobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile == null ? null : mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    /**
     * IP 脱敏：末段打码，如 192.168.1.***；IPv6 取前段 + :***
     */
    public static String ip(String ip) {
        if (ip == null || ip.isBlank()) {
            return ip;
        }
        // IPv6（含 ::1 等）
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1] + ":***";
            }
            return "***";
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot <= 0) {
            return ip;
        }
        return ip.substring(0, lastDot + 1) + "***";
    }
}

