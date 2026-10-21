package com.keryo.training.modules.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keryo.training.modules.training.entity.Training;
import com.keryo.training.modules.training.mapper.TrainingMapper;
import com.keryo.training.modules.training.service.TrainingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class TrainingServiceImpl extends ServiceImpl<TrainingMapper, Training> implements TrainingService {
}

