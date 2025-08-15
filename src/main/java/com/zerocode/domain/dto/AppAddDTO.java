package com.zerocode.domain.dto;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import lombok.Data;

@Data
public class AppAddDTO {

    /**
     * 应用初始化提示词
     */
    private String initPrompt;

    /**
     * 应用生成类型
     */
    private GeneratorTypeEnum generateType = GeneratorTypeEnum.HTML;

}
