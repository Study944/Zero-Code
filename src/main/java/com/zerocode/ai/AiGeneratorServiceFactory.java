package com.zerocode.ai;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ai服务工厂类
 */
@Configuration
public class AiGeneratorServiceFactory {

    @Resource
    private OllamaChatModel chatModel;

    @Resource
    private OllamaStreamingChatModel streamingChatModel;

    /**
     * 调用AIService创建Ai服务
     * @return Ai服务
     */
    @Bean
    public AiGeneratorService getAiGenerateService() {
        //return AiServices.create(AiGeneratorService.class, chatModel);
        return AiServices.builder(AiGeneratorService.class)
                .chatLanguageModel(chatModel)
                .streamingChatLanguageModel(streamingChatModel)
                .build();
    }
}
