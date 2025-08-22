package com.zerocode.ai.aiservices;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.ai.entity.HtmlCodeResult;
import com.zerocode.ai.entity.MultiFileCodeResult;
import dev.langchain4j.service.*;
import reactor.core.publisher.Flux;

public interface AiGeneratorService {

    /**
     * 生成HTML单文件格式
     * @param userPrompt 用户输入
     * @return  HTML
     */
    @SystemMessage(fromResource = "prompt/code-html-system-prompt.txt")
    HtmlCodeResult generateHtml(String userPrompt);

    /**
     * 生成多文件格式
     * @param userPrompt 用户输入
     * @return  多文件格式
     */
    @SystemMessage(fromResource = "prompt/code-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFile(String userPrompt);

    /**
     * 流式生成HTML单文件格式
     * @param userPrompt 用户输入
     * @return Flux<String>
     */
    @SystemMessage(fromResource = "prompt/code-html-system-prompt.txt")
    Flux<String> generateHtmlStream(String userPrompt);

    /**
     * 流式生成多文件格式
     * @param userPrompt 用户输入
     * @return Flux<String>
     */
    @SystemMessage(fromResource = "prompt/code-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileStream(String userPrompt);

    /**
     * 生成Vue项目
     * @param appId
     * @param userPrompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/code-vue-project-system-prompt.txt")
    TokenStream generateVueProjectStream(@MemoryId Long appId, @UserMessage String userPrompt);

    /**
     * 生成React 项目
     * @param appId
     * @param userPrompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/code-react-project-system-prompt.txt")
    TokenStream generateReactProjectStream(@MemoryId Long appId,@UserMessage String userPrompt);

    /**
     * 智能项目生成类型路由
     * @param userPrompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/code-routing-system-prompt.txt")
    GeneratorTypeEnum generateRouting(String userPrompt);

}
