package com.keryo.training.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keryo.training.modules.user.entity.ForgetPass;
import com.keryo.training.modules.user.mapper.ForgetPassMapper;
import com.keryo.training.modules.user.service.ForgetPassService;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import com.yf.system.modules.user.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 忘记密码申请记录 Service 实现
 */
@Service
public class ForgetPassServiceImpl extends ServiceImpl<ForgetPassMapper, ForgetPass> implements ForgetPassService {

    @Override
    public IPage<ForgetPass> paging(PagingReqDTO<ForgetPass> reqDTO) {
        LambdaQueryWrapper<ForgetPass> q = new LambdaQueryWrapper<>();
        ForgetPass params = reqDTO.getParams();
        if (params != null) {
            if (StringUtils.isNotBlank(params.getUserName())) {
                q.like(ForgetPass::getUserName, params.getUserName());
            }
            if (StringUtils.isNotBlank(params.getRealName())) {
                q.like(ForgetPass::getRealName, params.getRealName());
            }
            if (params.getStatus() != null) {
                q.eq(ForgetPass::getStatus, params.getStatus());
            }
        }
        q.orderByDesc(ForgetPass::getCreateTime);
        return this.page(new Page<>(reqDTO.getCurrent(), reqDTO.getSize()), q);
    }

    @Override
    public void handle(String id, String remark) {
        ForgetPass record = this.getById(id);
        if (record == null) {
            throw new ServiceException("申请记录不存在！");
        }
        if (Integer.valueOf(1).equals(record.getStatus())) {
            throw new ServiceException("该申请已处理，无需重复操作！");
        }
        record.setStatus(1);
        record.setRemark(remark);
        record.setHandleBy(UserUtils.getUserId());
        record.setHandleTime(new Date());
        this.updateById(record);
    }
}

