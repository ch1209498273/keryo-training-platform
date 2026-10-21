package com.keryo.training.modules.stat.service;

import com.keryo.training.modules.stat.entity.DashboardDTO;

/**
 * 统计看板服务（T-10）
 */
public interface DashboardService {

    /** 管理端仪表盘（data_scope 过滤） */
    DashboardDTO dashboard();

    /** 学员端我的概览 */
    DashboardDTO myOverview();
}

