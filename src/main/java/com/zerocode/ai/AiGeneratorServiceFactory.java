package com.zerocode.ai;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
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

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    /**
     * 调用AIService创建Ai服务
     * @return Ai服务
     */
    @Bean
    public AiGeneratorService getAiGenerateService() {
        return AiServices.builder(AiGeneratorService.class)
                .chatLanguageModel(chatModel)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemoryProvider(messageId-> MessageWindowChatMemory
                        .builder()
                        .chatMemoryStore(redisChatMemoryStore)
                        .id(messageId)
                        .maxMessages(10)
                        .build()
                )
                .build();
    }
}
