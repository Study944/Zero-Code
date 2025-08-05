package com.zerocode.domain.enums;

import lombok.Getter;

/**
 * 用户权限枚举类
 */
@Getter
public enum UserEnums {
    ADMIN("管理员","admin"),
    USER("普通用户","user")
    ;
    // 权限名字
    private final String name;
    // 权限值
    private final String value;

    UserEnums(String name, String value){
        this.name = name;
        this.value = value;
    }
    // 根据value获取枚举
    public static UserEnums getEnumByValue(String value){
        if (value == null){
            return null;
        }
        for (UserEnums userEnums : UserEnums.values()) {
            if (userEnums.value.equals(value)){
                return userEnums;
            }
        }
        return null;
    }
}
