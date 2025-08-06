package com.zerocode.domain.dto;

import com.zerocode.ai.GeneratorTypeEnum;
import lombok.Data;

@Data
public class AppGenerateCodeDTO {

    private Long appId;

    private String userPrompt;

    private GeneratorTypeEnum generatorTypeEnum = GeneratorTypeEnum.HTML;
}
