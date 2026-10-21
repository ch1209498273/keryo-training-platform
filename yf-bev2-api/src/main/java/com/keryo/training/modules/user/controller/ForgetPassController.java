package com.keryo.training.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keryo.training.modules.user.entity.ForgetPass;
import com.keryo.training.modules.user.service.ForgetPassService;
import com.yf.base.api.api.ApiRest;
import com.yf.base.api.api.controller.BaseController;
import com.yf.base.api.api.dto.PagingReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 忘记密码申请记录管理（管理端）
 */
@Tag(name = "忘记密码申请管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/keryo/forget-pass")
public class ForgetPassController extends BaseController {

    private final ForgetPassService forgetPassService;

    @Operation(summary = "分页查询申请记录")
    @RequiresPermissions("sys:user:forget-paging")
    @PostMapping("/paging")
    public ApiRest<IPage<ForgetPass>> paging(@RequestBody PagingReqDTO<ForgetPass> reqDTO) {
        return success(forgetPassService.paging(reqDTO));
    }

    @Operation(summary = "标记已处理")
    @RequiresPermissions("sys:user:forget-handle")
    @PostMapping("/handle")
    public ApiRest<?> handle(@RequestBody Map<String, Object> req) {
        String id = (String) req.get("id");
        if (id == null || id.isBlank()) {
            throw new com.yf.base.api.exception.ServiceException("记录ID不能为空！");
        }
        String remark = (String) req.get("remark");
        forgetPassService.handle(id, remark);
        return success();
    }
}

