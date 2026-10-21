package com.keryo.training.modules.stat.controller;

import com.keryo.training.modules.stat.entity.DashboardDTO;
import com.keryo.training.modules.stat.service.DashboardService;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计看板（T-10）
 * 管理端 overview 仅管理角色（permission_tag=keryo:stat:overview）；学员端 mine 登录即可
 */
@RestController
@RequestMapping("/api/keryo/dashboard")
@RequiredArgsConstructor
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    @Operation(summary = "管理端仪表盘（data_scope）")
    @RequiresPermissions("keryo:stat:overview")
    @PostMapping("/overview")
    public ApiRest<DashboardDTO> overview() {
        return super.success(dashboardService.dashboard());
    }

    @Operation(summary = "学员端我的概览")
    @PostMapping("/mine")
    public ApiRest<DashboardDTO> mine() {
        return super.success(dashboardService.myOverview());
    }
}

