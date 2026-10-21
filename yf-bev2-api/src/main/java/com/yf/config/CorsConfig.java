package com.yf.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 网关全局设置，允许跨域
 *
 * @author bool
 * @date 2019-08-13 17:28
 */

@Configuration
public class CorsConfig {

    @Value("${keryo.security.allowed-origins:http://localhost:8000,http://127.0.0.1:8000}")
    private String allowedOrigins;

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
        config.setAllowedOrigins(origins);
        config.setAllowedHeaders(Arrays.asList(
                "token", "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH", "OPTIONS"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}

