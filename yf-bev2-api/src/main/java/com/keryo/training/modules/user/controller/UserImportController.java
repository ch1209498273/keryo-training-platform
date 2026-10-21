package com.keryo.training.modules.user.controller;

import com.yf.base.api.api.ApiRest;
import com.keryo.training.common.security.FileSecurityUtil;
import com.keryo.training.common.security.PasswordPolicyService;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.mapper.SysUserMapper;
import com.yf.system.modules.user.service.SysUserRoleService;
import com.yf.base.utils.passwd.PassHandler;
import com.yf.base.utils.passwd.PassInfo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 批量用户导入
 */
@Log4j2
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class UserImportController {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleService sysUserRoleService;
    private final PasswordPolicyService passwordPolicyService;

    /**
     * Import credentials are deployment configuration, never a source-code default.
     * Imported accounts keep lastPasswordTime unset and therefore enter the temporary
     * password-change flow on first login.
     */
    @Value("${keryo.security.user-import-initial-password:}")
    private String configuredImportInitialPassword;

    @Operation(summary = "批量导入用户")
    @RequiresRoles("admin")
    @PostMapping("/batch-import")
    @Transactional
    public ApiRest<Map<String, Object>> batchImport(@RequestParam("file") MultipartFile file) {
        validateImportInitialPassword();
        int success = 0, fail = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook wb = FileSecurityUtil.safeExcel(file)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String name   = getCell(row, 0);
                    String phone  = getCell(row, 1);
                    String role   = getCell(row, 2);
                    String center = getCell(row, 3);

                    SysUser user = new SysUser();
                    user.setId(UUID.randomUUID().toString().replace("-", ""));
                    user.setUserName(phone);
                    user.setRealName(name);
                    user.setMobile(phone);
                    user.setDeptCode(center);
                    user.setState(0);
                    PassInfo pi = PassHandler.buildPassword(configuredImportInitialPassword);
                    user.setPassword(pi.getPassword());
                    user.setSalt(pi.getSalt());
                    sysUserMapper.insert(user);

                    // 分配角色
                    Map<String, String> roleMap = Map.of(
                        "一线医护", "keryo_nurse",
                        "医护长",   "keryo_head_nurse",
                        "区域督导", "keryo_supervisor"
                    );
                    String roleId = roleMap.getOrDefault(role, "keryo_nurse");
                    sysUserRoleService.saveRoles(user.getId(), List.of(roleId), false);

                    success++;
                } catch (Exception e) {
                    fail++;
                    errors.add("行" + (i+1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("文件解析失败: " + e.getMessage());
        }

        ApiRest<Map<String, Object>> resp = new ApiRest<>();
        resp.setCode(0);
        Map<String, Object> data = Map.of("success", success, "fail", fail, "errors", errors);
        resp.setData(data);
        return resp;
    }

    private void validateImportInitialPassword() {
        if (configuredImportInitialPassword == null || configuredImportInitialPassword.isBlank()) {
            throw new IllegalStateException(
                    "用户导入初始密码未配置，请设置 KERYO_SECURITY_USER_IMPORT_INITIAL_PASSWORD");
        }
        passwordPolicyService.validateStrength(configuredImportInitialPassword);
        if (!configuredImportInitialPassword.matches("[A-Za-z0-9]+")
                || !configuredImportInitialPassword.matches("(?=.*[A-Za-z])(?=.*\\d).+")) {
            throw new IllegalStateException("用户导入初始密码必须只包含字母和数字，且同时包含字母和数字");
        }
    }

    private String getCell(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}

