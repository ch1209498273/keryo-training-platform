package com.yf.ability.shiro;


import com.yf.ability.shiro.dto.SysUserLoginDTO;
import com.yf.ability.shiro.jwt.JwtToken;
import com.yf.ability.shiro.jwt.JwtUtils;
import com.yf.ability.shiro.service.ShiroUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

/**
 * 用户登录鉴权和获取用户授权
 *
 * @author bool
 */
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Component
@Slf4j
public class MyShiroRealm extends AuthorizingRealm {

    private final ShiroUserService shiroUserService;


    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }


    /**
     * 详细授权认证
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.info("++++++++++开始校验详细权限");

        log.info("++++++++详细权限校验。。。。");

        String userId = null;
        if (principals != null) {
            SysUserLoginDTO user = (SysUserLoginDTO) principals.getPrimaryPrincipal();
            userId = user.getId();
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // 查找用户角色
        List<String> roles = shiroUserService.roles(userId);
        info.setRoles(new HashSet<>(roles));

        // 查找权限明细
        List<String> permissions = shiroUserService.permissions(userId);
        info.addStringPermissions(permissions);

        log.info("++++++++++校验详细权限完成");
        return info;
    }

    /**
     * 校验用户的账号密码是否正确
     *
     * @param auth
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        if (token == null) {
            throw new AuthenticationException("token为空!");
        }
        // 校验token有效性
        SysUserLoginDTO user = this.checkToken(token);

        return new SimpleAuthenticationInfo(user, token, getName());
    }


    /**
     * 校验Token的有效性
     *
     * @param token
     * @return
     * @throws AuthenticationException
     */
    public SysUserLoginDTO checkToken(String token) throws AuthenticationException {

        try {
            String username = JwtUtils.getUsername(token);
            if (username.endsWith(":refresh")) {
                throw new AuthenticationException("刷新令牌不能用于访问业务接口");
            }

            // 只有签名验证通过后才读取 Redis/数据库中的会话身份。
            SysUserLoginDTO user = shiroUserService.token(token);
            if (user == null || !username.equals(user.getUserName())) {
                throw new AuthenticationException("会话失效，请重新登录!");
            }
            if (!Integer.valueOf(0).equals(user.getState())) {
                throw new AuthenticationException("账号当前不可登录");
            }
            return user;
        } catch (Exception e) {
            if (e instanceof AuthenticationException authenticationException) {
                throw authenticationException;
            }
            throw new AuthenticationException("无效的token", e);
        }
    }

    /**
     * 清除当前用户的权限认证缓存
     *
     * @param principals
     */
    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

}

