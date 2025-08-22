package com.zerocode.ai.aiservices;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import dev.langchain4j.service.SystemMessage;

public interface AiGeneratorRoutingService {

    /**
     * 智能项目生成类型路由
     * @param userPrompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/code-routing-system-prompt.txt")
    GeneratorTypeEnum generateRouting(String userPrompt);

}
