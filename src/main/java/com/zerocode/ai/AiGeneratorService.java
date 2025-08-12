package com.zerocode.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiGeneratorService {

    /**
     * 生成HTML单文件格式
     * @param userPrompt 用户输入
     * @return  HTML
     */
    @SystemMessage(fromResource = "prompt/code-html-system-prompt.txt")
    HtmlCodeResult generateHtml(@MemoryId Long messageId, @UserMessage String userPrompt);

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
    Flux<String> generateHtmlStream(@MemoryId Long messageId,@UserMessage String userPrompt);

    /**
     * 流式生成多文件格式
     * @param userPrompt 用户输入
     * @return Flux<String>
     */
    @SystemMessage(fromResource = "prompt/code-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileStream(String userPrompt);

}
