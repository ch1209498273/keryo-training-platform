package com.keryo.training.modules.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keryo.training.modules.audit.dto.AuditLogQueryReqDTO;
import com.keryo.training.modules.audit.entity.AuditLog;
import com.yf.base.api.api.dto.PagingReqDTO;

public interface AuditLogService extends IService<AuditLog> {

    /** Read-only paged query for the management page. */
    IPage<AuditLog> paging(PagingReqDTO<AuditLogQueryReqDTO> reqDTO);

    /** 记录审计日志（异步写入，不阻塞主流程） */
    void record(AuditLog entity);
}

