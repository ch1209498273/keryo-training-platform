package com.keryo.training.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keryo.training.modules.user.entity.ForgetPass;
import org.apache.ibatis.annotations.Mapper;

/**
 * 忘记密码申请记录 Mapper
 */
@Mapper
public interface ForgetPassMapper extends BaseMapper<ForgetPass> {
}

