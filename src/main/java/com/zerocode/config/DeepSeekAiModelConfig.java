package com.zerocode.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * deepseek模型配置
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.community.deepseek")
@Data
public class DeepSeekAiModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Long timeout;

    private Integer maxTokens;

    private Double temperature;

    private Boolean logRequests;

    private Boolean logResponses;

    @Bean
    @Scope("prototype")
    public OpenAiStreamingChatModel deepSeekStreamingChatModel() {

        return OpenAiStreamingChatModel.builder()
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

    @Bean
    @Scope("prototype")
    public OpenAiChatModel deepSeekChatModel() {
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
