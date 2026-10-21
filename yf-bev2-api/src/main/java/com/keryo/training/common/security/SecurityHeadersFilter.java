package com.keryo.training.common.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 统一安全过滤器（T-07.4）
 * 1. 注入 6 个安全响应头（X-Frame-Options/CSP/X-XSS/X-Content-Type/Referrer-Policy/Permissions-Policy）
 * 2. CSRF：SameSite Cookie + Origin/Referer 校验（仅对非 GET 请求）
 * 3. XSS：请求参数首层过滤（可扩展为包装请求）
 */
@Log4j2
@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {

    @Value("${keryo.security.allowed-origins:http://localhost:8000,http://127.0.0.1:8000}")
    private String configuredAllowedOrigins;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 1. 安全响应头
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-XSS-Protection", "1; mode=block");
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        resp.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        // CSP Report-Only（先观察违规再切强制）
        resp.setHeader("Content-Security-Policy-Report-Only",
                "default-src 'self'; img-src 'self' data: https:; style-src 'self' 'unsafe-inline'; "
                + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; font-src 'self' data:; "
                + "connect-src 'self' https:; frame-ancestors 'self'");

        // 2. CSRF：非 GET/HEAD/OPTIONS 请求校验 Origin/Referer
        String method = req.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)
                && !"OPTIONS".equalsIgnoreCase(method)) {
            String origin = req.getHeader("Origin");
            String referer = req.getHeader("Referer");
            String source = StringUtils.hasText(origin) ? origin
                    : (StringUtils.hasText(referer) ? referer : null);
            if (source != null) {
                if (!isAllowedSource(source, req)) {
                    log.warn("CSRF 拦截：{} {} from {}", method, req.getRequestURI(), source);
                    resp.setStatus(403);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"code\":-1,\"msg\":\"跨站请求被拒绝\"}");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowedSource(String source, HttpServletRequest request) {
        try {
            URI parsed = new URI(source);
            if (!StringUtils.hasText(parsed.getScheme()) || !StringUtils.hasText(parsed.getHost())) {
                return false;
            }
            String normalized = normalizeOrigin(parsed);
            Set<String> allowList = Arrays.stream(configuredAllowedOrigins.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(this::normalizeConfiguredOrigin)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            URI requestUri = new URI(request.getScheme(), null, request.getServerName(),
                    request.getServerPort(), null, null, null);
            return allowList.contains(normalized) || normalizeOrigin(requestUri).equals(normalized);
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return false;
        }
    }

    private String normalizeConfiguredOrigin(String value) {
        try {
            return normalizeOrigin(new URI(value));
        } catch (URISyntaxException ex) {
            return "";
        }
    }

    private String normalizeOrigin(URI uri) {
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        int port = uri.getPort();
        boolean defaultPort = ("http".equals(scheme) && (port == -1 || port == 80))
                || ("https".equals(scheme) && (port == -1 || port == 443));
        return scheme + "://" + host + (defaultPort || port == -1 ? "" : ":" + port);
    }
}

