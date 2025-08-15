package com.zerocode.domain.dto;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import lombok.Data;

@Data
public class AppGenerateCodeDTO {

    private Long appId;

    private String userPrompt;

    private GeneratorTypeEnum generatorTypeEnum = GeneratorTypeEnum.VUE_PROJECT;
}
