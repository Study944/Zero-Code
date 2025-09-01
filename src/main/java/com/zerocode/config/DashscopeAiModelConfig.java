package com.zerocode.config;


import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 阿里云dashscope模型配置
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.community.dashscope")
@Data
public class DashscopeAiModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Long timeout;

    private Integer maxTokens;

    private Double temperature;

    private Boolean logRequests;

    private Boolean logResponses;

    @Bean
    // 开启多例模式，单例模式下同一时刻只有一个Model在工作
    @Scope("prototype")
    public OpenAiStreamingChatModel dashscopeStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(timeout))
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .strictJsonSchema(true)
                .build();
    }

    @Bean
    @Scope("prototype")
    public OpenAiChatModel dashscopeChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(timeout))
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
