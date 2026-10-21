package com.keryo.training.common.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.keryo.training.common.constant.KeryoConstants;
import com.yf.base.api.exception.ServiceException;
import com.yf.base.utils.passwd.PassHandler;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.entity.SysUserPasswordHistory;
import com.yf.system.modules.user.mapper.SysUserPasswordHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/** Password strength, reuse and expiration policy. */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final SysUserPasswordHistoryMapper passwordHistoryMapper;

    /** Validate the minimum strength required by the password policy. */
    public void validateStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new ServiceException("密码长度不能少于8位");
        }
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        if (!hasLetter || !hasDigit) {
            throw new ServiceException("密码必须包含字母和数字");
        }
    }

    /** Reject the current password and the most recent password history entries. */
    public void assertNotReused(String userId, String currentPasswordHash, String newPassword) {
        if (currentPasswordHash != null && !currentPasswordHash.isBlank()
                && !PassHandler.isLegacyMd5(currentPasswordHash)
                && matches(newPassword, currentPasswordHash)) {
            throw new ServiceException("新密码不能与最近密码重复");
        }

        QueryWrapper<SysUserPasswordHistory> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(SysUserPasswordHistory::getUserId, userId)
                .orderByDesc(SysUserPasswordHistory::getCreateTime)
                .last("LIMIT " + KeryoConstants.PASSWORD_HISTORY_SIZE);
        List<SysUserPasswordHistory> history = passwordHistoryMapper.selectList(wrapper);
        for (SysUserPasswordHistory item : history) {
            if (item.getPassword() != null && !PassHandler.isLegacyMd5(item.getPassword())
                    && matches(newPassword, item.getPassword())) {
                throw new ServiceException("新密码不能与最近"
                        + KeryoConstants.PASSWORD_HISTORY_SIZE + "次密码重复");
            }
        }
    }

    public void record(String userId, String passwordHash, Date createTime) {
        if (userId == null || passwordHash == null || passwordHash.isBlank()) {
            return;
        }
        SysUserPasswordHistory history = new SysUserPasswordHistory();
        history.setId(IdWorker.getIdStr());
        history.setUserId(userId);
        history.setPassword(passwordHash);
        history.setCreateTime(createTime == null ? new Date() : createTime);
        passwordHistoryMapper.insert(history);
    }

    public boolean requiresTemporaryChange(SysUser user) {
        Date lastPasswordTime = user.getLastPasswordTime();
        if (lastPasswordTime == null) {
            return true;
        }
        long expireAt = lastPasswordTime.getTime()
                + KeryoConstants.PASSWORD_EXPIRE_DAYS * 24L * 60L * 60L * 1000L;
        return expireAt <= System.currentTimeMillis();
    }

    public Integer warningDays(SysUser user) {
        Date lastPasswordTime = user.getLastPasswordTime();
        if (lastPasswordTime == null) {
            return null;
        }
        long remaining = lastPasswordTime.getTime()
                + KeryoConstants.PASSWORD_EXPIRE_DAYS * 24L * 60L * 60L * 1000L
                - System.currentTimeMillis();
        if (remaining <= 0) {
            return null;
        }
        int days = (int) Math.ceil(remaining / (24d * 60L * 60L * 1000L));
        return days <= KeryoConstants.PASSWORD_WARN_DAYS ? days : null;
    }

    private boolean matches(String rawPassword, String encodedPassword) {
        try {
            return PassHandler.checkPass(rawPassword, "", encodedPassword);
        } catch (Exception ignored) {
            return false;
        }
    }
}

