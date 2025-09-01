package com.zerocode.ai.aiservices;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zerocode.ai.guardrail.PromptSafetyInputGuardrail;
import com.zerocode.ai.guardrail.RetryOutputGuardrail;
import com.zerocode.ai.tool.*;
import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
import com.zerocode.service.ChatHistoryService;
import com.zerocode.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

/**
 * AIServices工厂类（初始化AIServices）
 */
@Configuration
public class AiGeneratorServiceFactory {

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;


    /**
     * 使用Caffeine缓存Ai服务，避免重复创建
     */
    private final Cache<Long, AiGeneratorService> aiServicesCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();


    /**
     * 获取Ai服务
     *
     * @param appId (每个应用对应一个AServices)
     * @return Ai服务
     */
    public AiGeneratorService getAiGeneratorService(Long appId, GeneratorTypeEnum generatorTypeEnum) {
        return aiServicesCache.get(appId, key -> aiGeneratorService(appId, generatorTypeEnum));
    }

    /**
     * 创建Ai服务
     *
     * @param appId
     * @return Ai服务
     */
    private AiGeneratorService aiGeneratorService(Long appId, GeneratorTypeEnum generatorTypeEnum) {
        // 初始化AI聊天历史缓存，使用Redis持久化
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory
                .builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(appId)
                .maxMessages(25)
                .build();
        // 从Mysql中加载AI聊天历史
        chatHistoryService.loadChatHistory(appId, messageWindowChatMemory);
        // 根据生成类型初始化不同的Ai服务
        switch (generatorTypeEnum) {
            case REACT_PROJECT, VUE_PROJECT -> {
                // 使用@Resource注解是单例模式，多例需要自己创建
                StreamingChatModel dashscopeStreamingChatModel = SpringContextUtil.getBean("dashscopeStreamingChatModel", StreamingChatModel.class);
                ChatModel dashscopeChatModel = SpringContextUtil.getBean("dashscopeChatModel", ChatModel.class);
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
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        //.outputGuardrails(new RetryOutputGuardrail())
                        .build();
            }
            case HTML, MULTI_FILE -> {
                // 使用@Resource注解是单例模式，多例需要自己创建
                StreamingChatModel dashscopeStreamingChatModel = SpringContextUtil.getBean("dashscopeStreamingChatModel", StreamingChatModel.class);
                return AiServices.builder(AiGeneratorService.class)
                        .streamingChatModel(dashscopeStreamingChatModel)
                        .chatMemoryProvider(messageId -> messageWindowChatMemory)
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        //.outputGuardrails(new RetryOutputGuardrail())
                        .build();
            }
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成模式" + generatorTypeEnum.getValue());
        }

    }

    /**
     * 调用AIService创建Ai服务 （旧）
     *
     * @return Ai服务
     */
    @Bean
    public AiGeneratorService aiGenerateService() {
        StreamingChatModel dashscopeStreamingChatModel = SpringContextUtil.getBean("dashscopeStreamingChatModel", StreamingChatModel.class);
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
