package com.zerocode.ai.aiservices;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zerocode.ai.tool.*;
import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
import com.zerocode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Ai服务工厂类
 */
@Configuration
public class AiGeneratorServiceFactory {

    @Resource
    private StreamingChatModel dashscopeStreamingChatModel;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private StreamingChatModel deepSeekStreamingChatModel;

    @Resource
    private ChatModel deepSeekChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;


    // Caffeine缓存AIService
    private final Cache<Long, AiGeneratorService> aiServicesCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();


    /**
     * 获取Ai服务
     *
     * @param appId
     * @return
     */
    public AiGeneratorService getAiGeneratorService(Long appId, GeneratorTypeEnum generatorTypeEnum) {
        return aiServicesCache.get(appId, key -> aiGeneratorService(appId, generatorTypeEnum));
    }

    /**
     * 创建Ai服务
     *
     * @param appId
     * @return
     */
    private AiGeneratorService aiGeneratorService(Long appId, GeneratorTypeEnum generatorTypeEnum) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory
                .builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(appId)
                .maxMessages(25)
                .build();
        chatHistoryService.loadChatHistory(appId, messageWindowChatMemory);
        switch (generatorTypeEnum) {
            case REACT_PROJECT, VUE_PROJECT -> {
                return AiServices.builder(AiGeneratorService.class)
                        .streamingChatModel(dashscopeStreamingChatModel)
                        .chatModel(dashscopeChatModel)
                        // 添加工具
                        .tools(toolManager.getAllTools())
                        // 处理AI调用不存在工具
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,
                                        toolExecutionRequest.name() + "工具不存在")
                        )
                        .chatMemoryProvider(messageId -> messageWindowChatMemory)
                        .build();
            }
            case HTML , MULTI_FILE -> {
                return AiServices.builder(AiGeneratorService.class)
                        .streamingChatModel(dashscopeStreamingChatModel)
                        .chatMemoryProvider(messageId -> messageWindowChatMemory)
                        .build();
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成模式"+generatorTypeEnum.getValue());
        }

    }

    /**
     * 调用AIService创建Ai服务
     *
     * @return Ai服务
     */
    @Bean
    public AiGeneratorService aiGenerateService() {
        return AiServices.builder(AiGeneratorService.class)
                .streamingChatModel(dashscopeStreamingChatModel)
                .chatMemoryProvider(messageId -> MessageWindowChatMemory
                        .builder()
                        .chatMemoryStore(redisChatMemoryStore)
                        .id(messageId)
                        .maxMessages(10)
                        .build()
                )
                .build();
    }
}
