package com.yf.system.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yf.ability.Constant;
import com.yf.ability.captcha.service.CaptchaService;
import com.yf.ability.redis.service.RedisService;
import com.yf.ability.shiro.dto.SysUserLoginDTO;
import com.yf.ability.shiro.jwt.JwtUtils;
import com.yf.ability.shiro.service.ShiroUserService;
import com.yf.base.api.api.ApiError;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import com.yf.base.utils.BeanMapper;
import com.yf.base.utils.CacheKey;
import com.yf.base.utils.jackson.JsonHelper;
import com.yf.base.utils.passwd.PassHandler;
import com.yf.base.utils.passwd.PassInfo;
import com.keryo.training.common.constant.KeryoConstants;
import com.keryo.training.common.security.PasswordPolicyService;
import com.keryo.training.common.security.SqlSafeUtil;
import com.yf.system.modules.config.enums.FuncSwitch;
import com.yf.system.modules.config.service.CfgSwitchService;
import com.yf.system.modules.menu.service.SysMenuService;
import com.yf.system.modules.role.entity.SysRole;
import com.yf.system.modules.user.UserUtils;
import com.yf.system.modules.user.dto.request.*;
import com.yf.system.modules.user.dto.response.UserListRespDTO;
import com.yf.system.modules.user.entity.SysUser;
import com.yf.system.modules.user.enums.SysRoleId;
import com.keryo.training.common.security.LoginProtectionService;
import com.yf.system.modules.user.enums.SysUserId;
import com.yf.system.modules.user.enums.UserState;
import com.yf.system.modules.user.mapper.SysUserMapper;
import com.yf.system.modules.user.service.SysUserRoleService;
import com.yf.system.modules.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 语言设置 服务实现类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2020-04-13 16:57
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService, ShiroUserService {

    private static final String TEMP_PASSWORD_KEY_PREFIX = "yf:password:temporary:";

    private final SysUserRoleService sysUserRoleService;

    private final RedisService redisService;

    private final CaptchaService captchaService;

    private final CfgSwitchService cfgSwitchService;

    private final SysMenuService sysMenuService;

    private final LoginProtectionService loginProtectionService;

    private final PasswordPolicyService passwordPolicyService;

    /** 部门 Mapper：校验学员选择所属中心是否为管理员已配置的中心 */
    @jakarta.annotation.Resource
    private com.yf.system.modules.depart.mapper.SysDepartMapper sysDepartMapper;

    /** 忘记密码申请记录 Mapper */
    @jakarta.annotation.Resource
    private com.keryo.training.modules.user.mapper.ForgetPassMapper forgetPassMapper;


    @Override
    public SysUserSaveReqDTO detail(String id) {

        // 基础信息复制
        SysUser user = this.getById(id);
        SysUserSaveReqDTO respDTO = new SysUserSaveReqDTO();
        BeanMapper.copy(user, respDTO);

        // 角色是要
        List<SysRole> roleList = sysUserRoleService.listRoles(user.getId());
        List<String> roles = new ArrayList<>();
        for (SysRole role : roleList) {
            roles.add(role.getId());
        }
        respDTO.setRoles(roles);

        // 清理掉密码
        respDTO.setPassword(null);
        respDTO.setSalt(null);

        return respDTO;
    }

    @Override
    public IPage<UserListRespDTO> paging(PagingReqDTO<SysUserQueryReqDTO> reqDTO) {
        // T-07.3 SQL注入防护：排序字段白名单校验（仅允许用户表安全列）
        if (reqDTO.getOrders() != null) {
            reqDTO.getOrders().removeIf(od ->
                    od.getColumn() == null || !SAFE_SORT_COLUMNS.contains(od.getColumn()));
        }
        // T-07.7：SQL 特殊字符过滤（防 jsqlparser 解析崩溃）
        if (reqDTO.getParams() != null) {
            if (reqDTO.getParams().getUserName() != null) {
                reqDTO.getParams().setUserName(SqlSafeUtil.sanitize(reqDTO.getParams().getUserName()));
            }
            if (reqDTO.getParams().getMobile() != null) {
                reqDTO.getParams().setMobile(SqlSafeUtil.sanitize(reqDTO.getParams().getMobile()));
            }
        }
        return baseMapper.paging(reqDTO.toPage(), reqDTO.getParams());
    }

    /** 用户表安全排序列（防 ${od.column} 注入） */
    private static final java.util.Set<String> SAFE_SORT_COLUMNS = java.util.Set.of(
            "create_time", "user_name", "real_name", "dept_code", "state", "id");

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        // 超级用户可以停用任何用户
        if(!SysUserId.ADMIN.equals(UserUtils.getUserId())){
            int count = sysUserRoleService.countWithLevel(ids, UserUtils.getRoleLevel());
            if (count < ids.size()) {
                throw new ServiceException("停用错误，可能存在越权操作！");
            }
        }

        if (ids.contains(UserUtils.getUserId())) {
            throw new ServiceException("您不可以停用自己的账号！");
        }

        // 用户记录必须保留，以便恢复账号并保留培训、考试和审计历史。
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .in(SysUser::getId, ids)
                .ne(SysUser::getUserName, "admin");
        SysUser record = new SysUser();
        record.setState(UserState.DISABLED);
        this.update(record, wrapper);
    }

    @Override
    public SysUserLoginDTO login(SysUserLoginReqDTO reqDTO) {

        if (reqDTO == null) {
            throw new ServiceException(ApiError.ERROR_10010001);
        }
        String username = StringUtils.trimToEmpty(reqDTO.getUserName());

        // 认证输入必须完整；空用户名/空密码不得进入用户查询或签发令牌路径。
        if (StringUtils.isBlank(username) || StringUtils.isBlank(reqDTO.getPassword())) {
            loginProtectionService.recordFail(username, "");
            throw new ServiceException(ApiError.ERROR_90010002);
        }

        // 登录保护：检查是否被锁定
        if (loginProtectionService.isLocked(username)) {
            throw new ServiceException(loginProtectionService.lockMessage(username));
        }

        // 当前业务策略：每次登录都必须完成图形验证码，后端不可被前端绕过
        if (StringUtils.isAnyBlank(reqDTO.getCaptchaKey(), reqDTO.getCaptchaValue())) {
            loginProtectionService.recordFail(username, "");
            throw new ServiceException("请先完成图形验证码验证！");
        }

        // 校验图形验证码
        boolean check = captchaService.checkCaptcha(reqDTO.getCaptchaKey(), reqDTO.getCaptchaValue());
        if (!check) {
            loginProtectionService.recordFail(username, "");
            throw new ServiceException("图形验证码不正确或已失效！");
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, username);
        SysUser user = this.getOne(wrapper, false);

        // 用户不存在也记录失败（防暴力枚举）
        if (user == null) {
            loginProtectionService.recordFail(username, "");
            throw new ServiceException(ApiError.ERROR_90010001);
        }

        // 校验用户状态&密码
        return this.checkAndLogin(user, reqDTO.getPassword());
    }


    /**
     * 用户登录校验
     *
     * @param user
     */
    private SysUserLoginDTO checkAndLogin(SysUser user, String password) {

        if (user == null) {
            throw new ServiceException(ApiError.ERROR_90010001);
        }

        // 被禁用
        if (UserState.DISABLED.equals(user.getState())) {
            throw new ServiceException(ApiError.ERROR_90010005);
        }

        // 待审核
        if (UserState.AUDIT.equals(user.getState())) {
            throw new ServiceException(ApiError.ERROR_90010006);
        }

        if (StringUtils.isBlank(password)) {
            loginProtectionService.recordFail(user.getUserName(), "");
            throw new ServiceException(ApiError.ERROR_90010002);
        }

        boolean pass = PassHandler.checkPass(password, user.getSalt(), user.getPassword());
        if (!pass) {
            loginProtectionService.recordFail(user.getUserName(), "");
            throw new ServiceException(ApiError.ERROR_90010002);
        }
        // T-07.1：存量 MD5 密码登录成功后自动迁移为 bcrypt
        if (PassHandler.isLegacyMd5(user.getPassword())) {
            try {
                PassInfo newPass = PassHandler.buildPassword(password);
                user.setPassword(newPass.getPassword());
                user.setSalt(newPass.getSalt());
                user.setLastPasswordTime(new Date());
                this.updateById(user);
                passwordPolicyService.record(user.getId(), user.getPassword(), user.getLastPasswordTime());
                log.info("用户[{}]密码已从 MD5 迁移为 bcrypt", user.getUserName());
            } catch (Exception e) {
                log.warn("密码迁移 bcrypt 失败（不影响登录）", e);
            }
        }

        // 登录成功——清空失败计数
        loginProtectionService.clearFail(user.getUserName());
        return this.setToken(user);
    }

    @Override
    public List<String> permissions(String userId) {
        return sysUserRoleService.findUserPermission(userId);
    }

    @Override
    public List<String> roles(String userId) {
        return sysUserRoleService.listRoleIds(userId);
    }

    @Override
    public SysUserLoginDTO token(String token) {

        // 获得会话
        String username;
        try {
            username = JwtUtils.getUsername(token);
        } catch (Exception e) {
            throw new ServiceException("会话失效，请重新登录！");
        }
        if (username.endsWith(":refresh")) {
            throw new ServiceException("刷新令牌不能用于访问业务接口");
        }

        log.error("++++++++用户名：{}", username);

        Map<String, Object> json = null;
        try {
            json = redisService.getJson(Constant.USER_NAME_KEY + username);
        } catch (Exception e) {
            log.warn("Redis 不可用，降级为数据库校验: {}", e.getMessage());
        }
        if (json != null) {
            SysUserLoginDTO cached = JsonHelper.parseObject(json, SysUserLoginDTO.class);
            if (cached == null || !UserState.NORMAL.equals(cached.getState())) {
                throw new ServiceException("账号当前不可登录");
            }
            return cached;
        }

        // 降级：Redis 不可用/无登录缓存 → 从数据库校验用户与角色（保证登录态不依赖 Redis）
        SysUser user = this.getOne(new QueryWrapper<SysUser>().lambda()
                .eq(SysUser::getUserName, username));
        if (user == null) {
            throw new ServiceException(ApiError.ERROR_10010002);
        }
        if (!UserState.NORMAL.equals(user.getState())) {
            throw new ServiceException("账号当前不可登录");
        }
        SysUserLoginDTO dto = new SysUserLoginDTO();
        BeanMapper.copy(user, dto);
        dto.setRoles(roles(user.getId()));
        return dto;
    }

    @CacheEvict(value = CacheKey.TOKEN, key = "#token")
    @Override
    public void logout(String token) {

        // 遵循T下线原则
        boolean tick = cfgSwitchService.isOn(FuncSwitch.LOGIN_TICK);
        if (tick) {
            try {
                String username = JwtUtils.getUsername(token);
                String[] keys = new String[]{Constant.USER_NAME_KEY + username};
                redisService.del(keys);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SysUserUpdateReqDTO reqDTO) {


        // 更新用户资料
        SysUser user = this.getById(UserUtils.getUserId());
        String previousPasswordHash = user.getPassword();

        // 所属中心由学员从管理员配置好的中心列表中选择；
        // 提交的 deptCode 非空时必须确实是已配置的中心（防伪造）
        String reqDept = reqDTO.getDeptCode();
        if (StringUtils.isNotBlank(reqDept) && !this.isConfiguredDept(reqDept)) {
            throw new ServiceException("所属中心不存在，请从中心列表中选择！");
        }
        BeanMapper.copy(reqDTO, user);

        // 修改标识
        boolean reLogin = false;

        // 修改密码
        String password = reqDTO.getPassword();
        if (!StringUtils.isBlank(password)) {
            // 与新注册同规则：只允许字母和数字、必须同时含字母和数字、长度≤30
            this.validatePassword(password);
            PassInfo passInfo = PassHandler.buildPassword(password);
            passwordPolicyService.assertNotReused(user.getId(), previousPasswordHash, password);
            user.setPassword(passInfo.getPassword());
            user.setSalt(passInfo.getSalt());
            user.setLastPasswordTime(new Date());
            reLogin = true;
        }

        // 重新登录
        if (reLogin) {
            // 退出登录
            String[] keys = new String[]{Constant.USER_NAME_KEY + user.getUserName()};
            redisService.del(keys);
        }

        // 更新信息
        this.updateById(user);

        if (reLogin) {
            passwordPolicyService.record(user.getId(), user.getPassword(), user.getLastPasswordTime());
        }

        this.setToken(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pass(SysUserPassReqDTO reqDTO) {

        // 旧密码不能与新密码一致
        boolean same = reqDTO.getOldPass().equals(reqDTO.getNewPass());
        if(same){
            throw new ServiceException("新密码不能与旧密码一样！");
        }

        // 新密码同样校验：只允许字母和数字、必须同时含字母和数字、长度≤30
        this.validatePassword(reqDTO.getNewPass());

        // 获取当前用户
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(SysUser::getId, SysUser::getPassword, SysUser::getSalt)
                .eq(SysUser::getId, UserUtils.getUserId());
        SysUser user = this.getOne(wrapper, false);

        // 旧密码不对
        boolean check = PassHandler.checkPass(reqDTO.getOldPass(), user.getSalt(), user.getPassword());
        if (!check) {
            throw new ServiceException(ApiError.ERROR_90010007);
        }

        passwordPolicyService.assertNotReused(user.getId(), user.getPassword(), reqDTO.getNewPass());

        PassInfo passInfo = PassHandler.buildPassword(reqDTO.getNewPass());
        user.setPassword(passInfo.getPassword());
        user.setSalt(passInfo.getSalt());
        user.setLastPasswordTime(new Date());
        this.updateById(user);
        passwordPolicyService.record(user.getId(), user.getPassword(), user.getLastPasswordTime());

    }

    @CacheEvict(value = CacheKey.MENU, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(SysUserSaveReqDTO reqDTO) {

        List<String> roles = reqDTO.getRoles();

        if (CollectionUtils.isEmpty(roles)) {
            throw new ServiceException(ApiError.ERROR_90010003);
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, reqDTO.getUserName());
        if (!StringUtils.isBlank(reqDTO.getId())) {
            wrapper.lambda().ne(SysUser::getId, reqDTO.getId());
        }


        long count = this.count(wrapper);
        if (count > 0) {
            throw new ServiceException("用户名不能重复！");
        }


        // 保存基本信息
        SysUser user;
        String previousPasswordHash = null;

        // 添加模式
        if (StringUtils.isBlank(reqDTO.getId())) {
            user = new SysUser();
            BeanMapper.copy(reqDTO, user);
            user.setId(IdWorker.getIdStr());
        } else {
            user = this.getById(reqDTO.getId());
            previousPasswordHash = user.getPassword();
            BeanMapper.copy(reqDTO, user);
        }

        // 级别
        int level = sysUserRoleService.findMaxLevel(reqDTO.getId());
        if (level > UserUtils.getRoleLevel()) {
            throw new ServiceException("越级操作，不能操作等级高的用户！");
        }


        // 修改密码
        boolean passwordChanged = false;
        if (!StringUtils.isBlank(reqDTO.getPassword())) {
            this.validatePassword(reqDTO.getPassword());
            passwordPolicyService.assertNotReused(user.getId(), previousPasswordHash, reqDTO.getPassword());
            PassInfo pass = PassHandler.buildPassword(reqDTO.getPassword());
            user.setPassword(pass.getPassword());
            user.setSalt(pass.getSalt());
            user.setLastPasswordTime(null);
            passwordChanged = true;
        }

        // 保存角色信息
        sysUserRoleService.saveRoles(user.getId(), roles, true);

        // 保存绑定关系
        this.saveOrUpdate(user);

        if (passwordChanged) {
            passwordPolicyService.record(user.getId(), user.getPassword(), new Date());
        }

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysUserLoginDTO reg(UserRegReqDTO reqDTO) {

        // 注册必填校验（前端仅拦截，后端必须强制，保证身份信息完整可用）
        if (StringUtils.isBlank(reqDTO.getUserName())) {
            throw new ServiceException("登录账号不能为空！");
        }
        if (StringUtils.isBlank(reqDTO.getRealName())) {
            throw new ServiceException("真实姓名不能为空！");
        }

        // 注册密码强校验：只允许字母和数字、必须同时含字母和数字、长度≤30
        this.validatePassword(reqDTO.getPassword());

        boolean check = captchaService.checkCaptcha(reqDTO.getCaptchaKey(), reqDTO.getCaptchaValue());
        if (!check) {
            throw new ServiceException("图形验证码不正确或已失效！");
        }


        // 功能开关
        boolean on = cfgSwitchService.isOn(FuncSwitch.USER_REG);
        if (!on) {
            throw new ServiceException("管理员未开启用户注册！");
        }


        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(SysUser::getId)
                .eq(SysUser::getUserName, reqDTO.getUserName());

        // 用户名即为手机号
        boolean exists = this.count(wrapper) > 0;
        if (exists) {
            throw new ServiceException("用户名已存在，换一个吧！！");
        }

        return this.saveAndLogin(
                null,
                reqDTO.getUserName(),
                reqDTO.getDeptCode(),
                reqDTO.getRealName(),
                null,
                null,
                "",
                reqDTO.getPassword());
    }

    /**
     * 忘记密码申请校验：图形验证码 + 用户名存在 + 姓名匹配，
     * 通过后写入申请记录，引导联系管理员重置（不直接改密码）
     */
    @Override
    public void forgetCheck(ForgetCheckReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ServiceException("忘记密码申请参数不能为空！");
        }
        if (StringUtils.isBlank(reqDTO.getUserName())) {
            throw new ServiceException("请输入用户名！");
        }
        if (StringUtils.isBlank(reqDTO.getRealName())) {
            throw new ServiceException("请输入真实姓名！");
        }
        // 图形验证码
        if (StringUtils.isAnyBlank(reqDTO.getCaptchaKey(), reqDTO.getCaptchaValue())) {
            throw new ServiceException("请先完成图形验证码验证！");
        }
        boolean check = captchaService.checkCaptcha(reqDTO.getCaptchaKey(), reqDTO.getCaptchaValue());
        if (!check) {
            throw new ServiceException("图形验证码不正确或已失效！");
        }
        // 校验用户名存在 + 姓名匹配
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, reqDTO.getUserName());
        SysUser user = this.getOne(wrapper, false);
        if (user == null) {
            throw new ServiceException("用户名不存在，请确认后重试！");
        }
        if (!StringUtils.equals(user.getRealName(), reqDTO.getRealName().trim())) {
            throw new ServiceException("真实姓名与用户名不匹配，请核对后重试！");
        }

        // 写入申请记录（待管理员处理）
        try {
            com.keryo.training.modules.user.entity.ForgetPass fp = new com.keryo.training.modules.user.entity.ForgetPass();
            fp.setUserId(user.getId());
            fp.setUserName(user.getUserName());
            fp.setRealName(user.getRealName());
            fp.setStatus(0);
            fp.setCreateTime(new Date());
            forgetPassMapper.insert(fp);
        } catch (Exception e) {
            log.error("忘记密码申请写入失败", e);
            throw new ServiceException("忘记密码申请暂时无法提交，请稍后重试");
        }
        log.info("用户[{}]提交忘记密码申请", user.getUserName());
    }


    /**
     * 保存用户并自动登录
     *
     * @param userName
     * @param deptCode
     * @param realName
     * @param mobile
     * @param avatar
     * @param password
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void temporaryChangePassword(TemporaryPasswordChangeReqDTO reqDTO) {
        if (reqDTO == null || StringUtils.isBlank(reqDTO.getToken())
                || StringUtils.isBlank(reqDTO.getNewPassword())
                || !StringUtils.equals(reqDTO.getNewPassword(), reqDTO.getConfirmPassword())) {
            throw new ServiceException("临时改密参数无效或两次密码不一致");
        }

        String token = reqDTO.getToken().trim();
        String key = TEMP_PASSWORD_KEY_PREFIX + token;
        String lockKey = key + ":lock";
        if (!redisService.tryLock(lockKey, 10_000L)) {
            throw new ServiceException("临时改密请求正在处理中，请稍后重试");
        }
        try {
            String payload = redisService.getString(key);
            if (StringUtils.isBlank(payload)) {
                throw new ServiceException("临时改密令牌无效或已过期");
            }
            String[] parts = payload.split("\\|", 2);
            if (parts.length != 2 || Long.parseLong(parts[1]) <= System.currentTimeMillis()) {
                redisService.del(key);
                throw new ServiceException("临时改密令牌无效或已过期");
            }

            SysUser user = this.getById(parts[0]);
            if (user == null) {
                throw new ServiceException("用户不存在");
            }
            this.validatePassword(reqDTO.getNewPassword());
            passwordPolicyService.assertNotReused(user.getId(), user.getPassword(), reqDTO.getNewPassword());

            PassInfo passInfo = PassHandler.buildPassword(reqDTO.getNewPassword());
            Date changedAt = new Date();
            user.setPassword(passInfo.getPassword());
            user.setSalt(passInfo.getSalt());
            user.setLastPasswordTime(changedAt);
            this.updateById(user);
            passwordPolicyService.record(user.getId(), user.getPassword(), changedAt);
            redisService.del(key);
        } catch (NumberFormatException e) {
            redisService.del(key);
            throw new ServiceException("临时改密令牌无效或已过期");
        } finally {
            redisService.unlock(lockKey);
        }
    }

    private String issueTemporaryPasswordChange(SysUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        long expiresAt = System.currentTimeMillis()
                + KeryoConstants.TEMP_PASSWORD_TOKEN_SECONDS * 1000L;
        redisService.set(TEMP_PASSWORD_KEY_PREFIX + token,
                user.getId() + "|" + expiresAt,
                (long) KeryoConstants.TEMP_PASSWORD_TOKEN_SECONDS);
        return token;
    }

    private SysUserLoginDTO saveAndLogin(String userId, String userName, String deptCode, String realName, String role, String mobile, String avatar, String password) {

        // 保存用户
        SysUser user = new SysUser();

        // 指定用户ID的
        if (!StringUtils.isBlank(userId)) {
            user.setId(userId);
        } else {
            user.setId(IdWorker.getIdStr());
        }

        // 指定部门
        boolean on = cfgSwitchService.isOn(FuncSwitch.USER_DEPT_TYPE);
        if (on) {
            deptCode = cfgSwitchService.val(FuncSwitch.USER_DEPT_CODE);
        }

        // 需要审核
        boolean audit = cfgSwitchService.isOn(FuncSwitch.USER_AUDIT);
        if (audit) {
            user.setState(UserState.AUDIT);
        } else {
            user.setState(UserState.NORMAL);
        }

        user.setUserName(userName);
        user.setRealName(realName);
        user.setDeptCode(deptCode);
        user.setMobile(mobile);
        user.setAvatar(avatar);
        PassInfo passInfo = PassHandler.buildPassword(password);
        user.setPassword(passInfo.getPassword());
        user.setSalt(passInfo.getSalt());

        // 保存角色
        List<String> roleList = new ArrayList<>();
        if (!StringUtils.isBlank(role)) {
            roleList.add(role);
        } else {
            // 默认学员角色（一线医护 keryo_nurse；原 user 角色已停用）
            roleList.add(SysRoleId.KERYO_NURSE);
        }


        // 保存角色
        sysUserRoleService.saveRoles(user.getId(), roleList, false);

        // 保存用户
        this.save(user);
        passwordPolicyService.record(user.getId(), user.getPassword(), new Date());

        return this.setToken(user);
    }

    /**
     * 校验是否为管理员已配置的中心（部门），用于学员选择所属中心
     */
    private boolean isConfiguredDept(String deptCode) {
        if (StringUtils.isBlank(deptCode)) {
            return false;
        }
        Long cnt = sysDepartMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                com.yf.system.modules.depart.entity.SysDepart>()
                .eq(com.yf.system.modules.depart.entity.SysDepart::getDeptCode, deptCode));
        return cnt != null && cnt > 0;
    }

    /**
     * 密码强校验（注册/改密码统一规则）：
     * 只允许字母和数字、必须同时包含字母和数字、长度≤30、不允许特殊字符
     */
    private void validatePassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new ServiceException("密码不能为空！");
        }
        if (password.length() > 30) {
            throw new ServiceException("密码长度不能超过30个字符！");
        }
        if (!password.matches("[A-Za-z0-9]+")) {
            throw new ServiceException("密码只能包含字母和数字，不允许特殊字符！");
        }
        if (!password.matches("(?=.*[A-Za-z])(?=.*\\d).+")) {
            throw new ServiceException("密码必须同时包含字母和数字！");
        }
        passwordPolicyService.validateStrength(password);
    }

    /**
     * 保存会话信息
     *
     * @param user
     * @return
     */
    private SysUserLoginDTO setToken(SysUser user) {

        // 获取一个用户登录的信息
        String key = Constant.USER_NAME_KEY + user.getUserName();
        String json = null;
        try { json = redisService.getString(key); } catch (Exception e) { log.warn("Redis不可用，跳过旧会话查询"); }
        if (!StringUtils.isBlank(json)) {
            // 删除旧的会话
            try { redisService.del(key); } catch (Exception e) { log.warn("Redis不可用，跳过旧会话删除"); }
        }

        SysUserLoginDTO respDTO = new SysUserLoginDTO();
        BeanMapper.copy(user, respDTO);
        respDTO.setPasswordExpireWarningDays(passwordPolicyService.warningDays(user));

        if (passwordPolicyService.requiresTemporaryChange(user)) {
            respDTO.setMustChangePassword(true);
            respDTO.setPasswordChangeToken(issueTemporaryPasswordChange(user));
            return respDTO;
        }

        // 正常状态才登录
        if (UserState.NORMAL.equals(user.getState())) {

            // 根据用户生成Token
            String token = JwtUtils.sign(user.getUserName(), 15 * 60);
            respDTO.setToken(token);

            // 刷新令牌（7天）
            String refreshToken = JwtUtils.sign(user.getUserName() + ":refresh", 7 * 24 * 3600);
            respDTO.setRefreshToken(refreshToken);

            // 添加角色信息
            this.fillRoleData(respDTO);

            // 权限表，用于前端控制按钮
            List<String> permissions = sysMenuService.listPermissionByRoles(respDTO.getRoles());
            respDTO.setPermissions(permissions);


            // 保存如Redis
            try { redisService.set(key, JsonHelper.toJson(respDTO)); } catch (Exception e) { log.warn("Redis不可用，会话仅存内存"); }
        }

        return respDTO;

    }


    /**
     * 追加用户角色信息
     *
     * @param respDTO
     */
    private void fillRoleData(SysUserLoginDTO respDTO) {

        // 角色是要
        List<SysRole> roleList = sysUserRoleService.listRoles(respDTO.getId());
        // 角色级别
        Integer roleLevel = 0;
        // 数据权限1最小：查看自己的数据
        Integer dataScope = 1;

        List<String> roleIds = new ArrayList<>();
        for (SysRole role : roleList) {
            // 角色ID
            roleIds.add(role.getId());
            // 替换大的权限
            if (dataScope < role.getDataScope()) {
                dataScope = role.getDataScope();
            }
            // 权限级别
            if (roleLevel < role.getRoleLevel()) {
                roleLevel = role.getRoleLevel();
            }
        }
        respDTO.setRoleLevel(roleLevel);
        respDTO.setDataScope(dataScope);
        respDTO.setRoles(roleIds);
    }
}

