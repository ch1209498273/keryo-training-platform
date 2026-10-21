package com.keryo.training.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keryo.training.modules.user.entity.ForgetPass;
import com.yf.base.api.api.dto.PagingReqDTO;

/**
 * 忘记密码申请记录 Service
 */
public interface ForgetPassService extends IService<ForgetPass> {

    /**
     * 分页查询申请记录
     */
    IPage<ForgetPass> paging(PagingReqDTO<ForgetPass> reqDTO);

    /**
     * 标记已处理
     *
     * @param id 记录ID
     * @param remark 处理备注（可选）
     */
    void handle(String id, String remark);
}

