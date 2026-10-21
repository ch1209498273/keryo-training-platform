package com.keryo.training.modules.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keryo.training.modules.audit.dto.AuditLogQueryReqDTO;
import com.keryo.training.modules.audit.entity.AuditLog;
import com.keryo.training.modules.audit.mapper.AuditLogMapper;
import com.keryo.training.modules.audit.service.AuditLogService;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j2
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public IPage<AuditLog> paging(PagingReqDTO<AuditLogQueryReqDTO> reqDTO) {
        LambdaQueryWrapper<AuditLog> query = new LambdaQueryWrapper<>();
        AuditLogQueryReqDTO params = reqDTO.getParams();
        if (params != null) {
            if (StringUtils.isNotBlank(params.getUserName())) {
                query.like(AuditLog::getUserName, params.getUserName().trim());
            }
            if (StringUtils.isNotBlank(params.getRealName())) {
                query.like(AuditLog::getRealName, params.getRealName().trim());
            }
            if (StringUtils.isNotBlank(params.getModule())) {
                query.like(AuditLog::getModule, params.getModule().trim());
            }
            if (StringUtils.isNotBlank(params.getOperation())) {
                query.like(AuditLog::getOperation, params.getOperation().trim());
            }
            if (StringUtils.isNotBlank(params.getResult())) {
                query.eq(AuditLog::getResult, params.getResult().trim());
            }
            if (StringUtils.isNotBlank(params.getCreateTimeStart())) {
                query.ge(AuditLog::getCreateTime, parseDate(params.getCreateTimeStart()));
            }
            if (StringUtils.isNotBlank(params.getCreateTimeEnd())) {
                query.le(AuditLog::getCreateTime, parseDate(params.getCreateTimeEnd()));
            }
        }
        query.orderByDesc(AuditLog::getCreateTime).orderByDesc(AuditLog::getId);
        return this.page(new Page<>(reqDTO.getCurrent(), reqDTO.getSize()), query);
    }

    @Override
    @Async("auditLogExecutor")
    public void record(AuditLog entity) {
        try {
            if (entity.getCreateTime() == null) {
                entity.setCreateTime(new Date());
            }
            this.save(entity);
        } catch (Exception e) {
            // 审计日志写入失败不影响主流程
            log.warn("审计日志写入失败", e);
        }
    }

    private Date parseDate(String value) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_PATTERN);
        format.setLenient(false);
        try {
            return format.parse(value.trim());
        } catch (ParseException e) {
            throw new ServiceException("时间格式必须为 yyyy-MM-dd HH:mm:ss");
        }
    }
}

