package com.zerocode.core;

import com.zerocode.ai.AiGeneratorService;
import com.zerocode.ai.GeneratorTypeEnum;
import com.zerocode.ai.HtmlCodeResult;
import com.zerocode.ai.MultiFileCodeResult;
import com.zerocode.core.parser.CodeParser;
import com.zerocode.core.parser.CodeParserExecutor;
import com.zerocode.core.saver.CodeFileSaver;
import com.zerocode.core.saver.CodeFileSaverExecutor;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
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
    private AiGeneratorService aiGeneratorService;

    /**
     * 生成代码并保存
     *
     * @param userPrompt        用户输入
     * @param generatorTypeEnum 生成类型
     * @return 文件
     */
    public File generateAndSaveCode(String userPrompt, GeneratorTypeEnum generatorTypeEnum, Long appId) {
        switch (generatorTypeEnum) {
            case HTML:
                HtmlCodeResult htmlCodeResult = aiGeneratorService.generateHtml(10086L,userPrompt);
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
        switch (generatorTypeEnum) {
            case HTML:
                // 生成响应式对象Flux
                Flux<String> htmlStream = aiGeneratorService.generateHtmlStream(appId,userPrompt);
                // 拼接返回
                return saveStreamCode(htmlStream, generatorTypeEnum,appId);
            case MULTI_FILE:
                // 生成响应式对象Flux
                Flux<String> multiFileStream = aiGeneratorService.generateMultiFileStream(userPrompt);
                // 拼接返回
                return saveStreamCode(multiFileStream, generatorTypeEnum,appId);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成模式" + generatorTypeEnum.getValue());
        }
    }

    /**
     * 保存流式代码工具
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
