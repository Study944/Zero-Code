package com.zerocode.ai.entity;

import lombok.Getter;

/**
 * 生成器类型枚举
 */
@Getter
public enum GeneratorTypeEnum {
    HTML("原生HTML模式", "html"),
    MULTI_FILE("原生多文件模式", "multi-file"),
    VUE_PROJECT("Vue项目模式", "vue-project"),
    REACT_PROJECT("React项目模式", "react-project"),
    ;

    private final String text;
    private final String value;

    GeneratorTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static GeneratorTypeEnum getByValue(String value) {
        for (GeneratorTypeEnum type : GeneratorTypeEnum.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
