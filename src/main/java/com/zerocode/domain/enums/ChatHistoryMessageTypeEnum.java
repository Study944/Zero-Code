package com.zerocode.domain.enums;

import lombok.Getter;

/**
 * 对话类型枚举类
 */
@Getter
public enum ChatHistoryMessageTypeEnum {
    AI("AI","ai"),
    USER("用户","user")
    ;
    // 权限名字
    private final String name;
    // 权限值
    private final String value;

    ChatHistoryMessageTypeEnum(String name, String value){
        this.name = name;
        this.value = value;
    }
    // 根据value获取枚举
    public static ChatHistoryMessageTypeEnum getEnumByValue(String value){
        if (value == null){
            return null;
        }
        for (ChatHistoryMessageTypeEnum userEnums : ChatHistoryMessageTypeEnum.values()) {
            if (userEnums.value.equals(value)){
                return userEnums;
            }
        }
        return null;
    }
}
