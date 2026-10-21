package com.keryo.training.modules.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keryo.training.modules.training.entity.Training;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainingMapper extends BaseMapper<Training> {
}

