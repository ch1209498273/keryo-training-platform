package com.keryo.training.modules.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解（T-07.2）：标注在 Controller 方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 模块名，如：培训计划/考试管理 */
    String module() default "";

    /** 操作描述，如：创建培训计划/审批通过 */
    String operation() default "";
}

