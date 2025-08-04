package com.zerocode.core;

import com.zerocode.ai.AiGeneratorService;
import com.zerocode.ai.GeneratorTypeEnum;
import com.zerocode.ai.HtmlCodeResult;
import com.zerocode.ai.MultiFileCodeResult;
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
    public File generateAndSaveCode(String userPrompt, GeneratorTypeEnum generatorTypeEnum) {
        switch (generatorTypeEnum) {
            case HTML:
                HtmlCodeResult htmlCodeResult = aiGeneratorService.generateHtml(userPrompt);
                return CodeFileSaver.saveHtmlCode(htmlCodeResult, generatorTypeEnum);
            case MULTI_FILE:
                MultiFileCodeResult multiFileCodeResult = aiGeneratorService.generateMultiFile(userPrompt);
                return CodeFileSaver.saveMultiFileCode(multiFileCodeResult, generatorTypeEnum);
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
    public Flux<String> generateAndSaveStreamCode(String userPrompt, GeneratorTypeEnum generatorTypeEnum) {
        switch (generatorTypeEnum) {
            case HTML:
                // 生成响应式对象Flux
                Flux<String> htmlStream = aiGeneratorService.generateHtmlStream(userPrompt);
                // 拼接为一个字符串
                StringBuilder htmlCodeBuilder = new StringBuilder();
                return htmlStream.doOnNext(string -> htmlCodeBuilder.append(string))
                        .doOnComplete(() -> {
                            // 拼接完成后，解析输出为 HtmlCodeResult 对象
                            HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(htmlCodeBuilder.toString());
                            // 文件写入
                            File file = CodeFileSaver.saveHtmlCode(htmlCodeResult, generatorTypeEnum);
                            log.info("文件保存成功：{}", file.getAbsolutePath());
                        });
            case MULTI_FILE:
                // 生成响应式对象Flux
                Flux<String> multiFileStream = aiGeneratorService.generateMultiFileStream(userPrompt);
                // 拼接为一个字符串
                StringBuilder multiFileCodeBuilder = new StringBuilder();
                return multiFileStream.doOnNext(string -> multiFileCodeBuilder.append(string))
                        .doOnComplete(() -> {
                            // 拼接完成后，解析输出为 MultiFileCodeResult 对象
                            MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(multiFileCodeBuilder.toString());
                            // 文件写入
                            File file = CodeFileSaver.saveMultiFileCode(multiFileCodeResult, generatorTypeEnum);
                            log.info("文件保存成功：{}", file.getAbsolutePath());
                        });
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成模式" + generatorTypeEnum.getValue());
        }
    }
}
