package com.keryo.training.common.enums;

import lombok.Getter;

/**
 * 数据权限范围，与 el_sys_role.data_scope 字段对应
 */
@Getter
public enum DataScopeEnum {
    ALL(4, "全部分中心数据"),
    CENTER_GROUP(3, "所辖多家中心数据"),
    CENTER(2, "本中心全部数据"),
    SELF(1, "仅本人数据");

    private final int code;
    private final String desc;

    DataScopeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataScopeEnum of(int code) {
        for (DataScopeEnum e : values()) {
            if (e.code == code) return e;
        }
        return SELF;
    }
}

