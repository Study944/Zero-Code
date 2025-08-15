package com.zerocode.ai.message;

import lombok.Getter;

@Getter
public enum StreamMessageTypeEnum {
    AI_RESPONSE("ai-response", "AI回复"),
    TOOL_REQUEST("tool-request", "工具请求"),
    TOOL_EXECUTED("tool-executed", "工具调用结果"),
    ;

    private final String type;
    private final String text;

    StreamMessageTypeEnum(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public static StreamMessageTypeEnum getByType(String type) {
        for (StreamMessageTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
