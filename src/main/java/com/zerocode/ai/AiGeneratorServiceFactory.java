package com.zerocode.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zerocode.domain.entity.ChatHistory;
import com.zerocode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

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

    @Resource
    private ChatHistoryService chatHistoryService;

    // Caffeine缓存AIService
    private final Cache<Long, AiGeneratorService> aiServicesCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();


    /**
     * 获取Ai服务
     * @param appId
     * @return
     */
    public AiGeneratorService getAiGeneratorService(Long appId) {
        return aiServicesCache.get(appId, this::aiGeneratorService);
    }

    /**
     * 创建Ai服务
     * @param appId
     * @return
     */
    private AiGeneratorService aiGeneratorService(Long appId) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory
                .builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(appId)
                .maxMessages(10)
                .build();
        chatHistoryService.loadChatHistory(appId, messageWindowChatMemory);
        return AiServices.builder(AiGeneratorService.class)
                .chatLanguageModel(chatModel)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemory(messageWindowChatMemory)
                .build();
    }

    /**
     * 调用AIService创建Ai服务
     * @return Ai服务
     */
    @Bean
    public AiGeneratorService aiGenerateService() {
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
