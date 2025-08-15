package com.zerocode.core;

import cn.hutool.json.JSONUtil;
import com.zerocode.ai.aiservices.AiGeneratorService;
import com.zerocode.ai.aiservices.AiGeneratorServiceFactory;
import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.ai.entity.HtmlCodeResult;
import com.zerocode.ai.entity.MultiFileCodeResult;
import com.zerocode.ai.message.AiResponseMessage;
import com.zerocode.ai.message.ToolExecutedMessage;
import com.zerocode.ai.message.ToolRequestMessage;
import com.zerocode.core.parser.CodeParserExecutor;
import com.zerocode.core.saver.CodeFileSaverExecutor;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServiceTokenStream;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成器门面类
 */
@Slf4j
@Service
public class CodeGeneratorFacade {

    @Resource
    private AiGeneratorServiceFactory aiGeneratorServiceFactory;

    /**
     * 生成代码并保存
     *
     * @param userPrompt        用户输入
     * @param generatorTypeEnum 生成类型
     * @return 文件
     */
    public File generateAndSaveCode(String userPrompt, GeneratorTypeEnum generatorTypeEnum, Long appId) {
        // 获取有记忆和缓存的AI服务
        AiGeneratorService aiGeneratorService = aiGeneratorServiceFactory.getAiGeneratorService(appId, generatorTypeEnum);
        switch (generatorTypeEnum) {
            case HTML:
                HtmlCodeResult htmlCodeResult = aiGeneratorService.generateHtml(userPrompt);
                return CodeFileSaverExecutor.executeSaver(htmlCodeResult, generatorTypeEnum, appId);
            case MULTI_FILE:
                MultiFileCodeResult multiFileCodeResult = aiGeneratorService.generateMultiFile(userPrompt);
                return CodeFileSaverExecutor.executeSaver(multiFileCodeResult, generatorTypeEnum, appId);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成模式" + generatorTypeEnum.getValue());
        }
    }

    /**
     * 生成流式代码拼接解析并保存
     *
     * @param userPrompt        用户输入
     * @param generatorTypeEnum 生成类型
     * @return Flux<String>响应式对象
     */
    public Flux<String> generateAndSaveStreamCode(String userPrompt, GeneratorTypeEnum generatorTypeEnum, Long appId) {
        // 获取有记忆和缓存的AI服务
        AiGeneratorService aiGeneratorService = aiGeneratorServiceFactory.getAiGeneratorService(appId, generatorTypeEnum);
        switch (generatorTypeEnum) {
            case HTML:
                // 生成响应式对象Flux
                Flux<String> htmlStream = aiGeneratorService.generateHtmlStream(userPrompt);
                // 拼接返回
                return saveStreamCode(htmlStream, generatorTypeEnum, appId);
            case MULTI_FILE:
                Flux<String> multiFileStream = aiGeneratorService.generateMultiFileStream(userPrompt);
                return saveStreamCode(multiFileStream, generatorTypeEnum, appId);
            case VUE_PROJECT:
                // 生成响应式对象 AiServiceTokenStream
                TokenStream vueProjectStream = aiGeneratorService.generateVueProjectStream(appId, userPrompt);
                // 解析为 Flux<String>
                return processTokenStream(vueProjectStream);
            case REACT_PROJECT:
                TokenStream reactProjectStream = aiGeneratorService.generateReactProjectStream(appId, userPrompt);
                return processTokenStream(reactProjectStream);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成模式" + generatorTypeEnum.getValue());
        }
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


    /**
     * 保存流式代码工具
     *
     * @param stream
     * @param generatorTypeEnum
     * @return
     */
    private Flux<String> saveStreamCode(Flux<String> stream, GeneratorTypeEnum generatorTypeEnum, Long appId) {
        // 拼接为一个字符串
        StringBuilder codeBuilder = new StringBuilder();
        return stream.doOnNext(string -> codeBuilder.append(string))
                .doOnComplete(() -> {
                    // 拼接完成后，解析输出为 MultiFileCodeResult 对象
                    Object executeParser = CodeParserExecutor.executeParser(codeBuilder.toString(), generatorTypeEnum);
                    // 文件写入
                    File file = CodeFileSaverExecutor.executeSaver(executeParser, generatorTypeEnum, appId);
                    log.info("文件保存成功：{}", file.getAbsolutePath());
                });
    }
}
