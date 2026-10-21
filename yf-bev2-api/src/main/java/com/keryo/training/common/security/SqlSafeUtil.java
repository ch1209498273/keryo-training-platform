package com.keryo.training.common.security;

/**
 * SQL 安全工具（T-07.7）
 * 过滤查询参数中的 SQL 特殊字符，防止 jsqlparser 数据权限拦截器解析崩溃
 * （注入本身已被 #{} 参数化阻止，此处解决框架解析器健壮性问题）
 */
public class SqlSafeUtil {

    private SqlSafeUtil() {
    }

    /** SQL 特殊字符正则：单引号/双引号/反斜杠/分号/括号/注释符 */
    private static final String SQL_SPECIAL = "['\"\\\\;()\\-\\-]";

    /**
     * 清洗字符串查询参数（保留中文/字母/数字/常见标点）
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll(SQL_SPECIAL, "").trim();
    }
}

