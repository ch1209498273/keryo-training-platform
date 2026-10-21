package com.keryo.training.modules.audit.aspect;

import com.keryo.training.modules.audit.annotation.AuditLog;
import com.keryo.training.modules.audit.service.AuditLogService;
import com.keryo.training.modules.audit.util.DesensitizeUtil;
import com.yf.system.modules.user.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 审计日志 AOP（T-07.2）
 * 记录：操作人/模块/操作/方法/URL/IP(脱敏)/参数(脱敏)/结果/耗时
 * 敏感字段自动脱敏：姓名留姓、手机号中间4位、IP末段
 */
@Log4j2
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String result = "SUCCESS";
        String errorMsg = null;
        Object returnValue = null;
        try {
            returnValue = joinPoint.proceed();
            return returnValue;
        } catch (Throwable e) {
            result = "FAIL";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                this.saveLog(joinPoint, auditLog, result, errorMsg, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("审计日志记录异常", e);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, AuditLog auditLog, String result,
                         String errorMsg, long costTime) {
        com.keryo.training.modules.audit.entity.AuditLog entity =
                new com.keryo.training.modules.audit.entity.AuditLog();
        // 操作人（脱敏）
        try {
            var user = UserUtils.getUser(false);
            if (user != null) {
                entity.setUserId(user.getId());
                entity.setUserName(user.getUserName());
                entity.setRealName(DesensitizeUtil.name(user.getRealName()));
            }
        } catch (Exception ignored) {
        }
        // 请求信息
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            entity.setMethod(request.getMethod());
            entity.setUrl(request.getRequestURI());
            entity.setIp(DesensitizeUtil.ip(getClientIp(request)));
        } else {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            entity.setMethod(signature.getMethod().getName());
        }
        entity.setModule(auditLog.module());
        entity.setOperation(auditLog.operation());
        entity.setParams(desensitizeParams(joinPoint.getArgs()));
        entity.setResult(result);
        entity.setErrorMsg(errorMsg == null ? null : errorMsg.length() > 500 ? errorMsg.substring(0, 500) : errorMsg);
        entity.setCostTime(costTime);
        auditLogService.record(entity);
    }

    /** 参数脱敏：过滤文件/密码/token 类敏感字段 */
    private String desensitizeParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            return Arrays.stream(args)
                    .filter(a -> !(a instanceof MultipartFile) && !(a instanceof HttpServletRequest)
                            && a != null)
                    .map(a -> String.valueOf(a).replaceAll("(?i)(password|token|captchaValue)\"?\\s*[:=]\\s*\"?[^\",}]*",
                            "$1=***"))
                    .collect(Collectors.joining(" | "))
                    .substring(0, Math.min(1500, 1999));
        } catch (Exception e) {
            return "[参数序列化失败]";
        }
    }

    /** 取客户端真实 IP（含代理链） */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

