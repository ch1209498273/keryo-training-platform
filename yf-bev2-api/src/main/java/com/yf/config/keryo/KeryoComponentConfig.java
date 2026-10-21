package com.yf.config.keryo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 启用医护培训平台扩展包组件扫描 + MyBatis Mapper 扫描
 */
@Configuration
@ComponentScan("com.keryo.training")
@MapperScan("com.keryo.training.**.mapper")
public class KeryoComponentConfig {
}

