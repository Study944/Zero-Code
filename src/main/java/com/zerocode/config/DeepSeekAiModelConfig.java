package com.zerocode.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.community.deepseek")
@Data
public class DeepSeekAiModelConfig {

    private String apiKey;

    @Bean
    public OpenAiStreamingChatModel deepSeekStreamingChatModel() {

        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-reasoner")
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(5))
                .maxTokens(8192)
                .logRequests(true)
                .logResponses(true)
                .strictJsonSchema(true)
                .responseFormat("json_object")
                .build();
    }

    @Bean
    public OpenAiChatModel deepSeekChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-reasoner") // 模型名称
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(5))
                .maxTokens(8192)
                .logRequests(true)
                .logResponses(true)
                .strictJsonSchema(true)
                .responseFormat("json_object")
                .build();
    }

}
