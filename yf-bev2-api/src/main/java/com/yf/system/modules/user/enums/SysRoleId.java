package com.yf.system.modules.user.enums;


/**
 * 默认角色信息
 *
 * @author bool
 * @date 2019-10-30 13:11
 */
public interface SysRoleId {

    /**
     * 超级管理员
     */
    String ADMIN = "admin";

    /**
     * 普通用户
     */
    String USER = "user";

    /**
     * 一线医护（普通学员，当前学员默认角色）
     */
    String KERYO_NURSE = "keryo_nurse";


}

