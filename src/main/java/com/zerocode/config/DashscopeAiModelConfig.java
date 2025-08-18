package com.zerocode.config;


import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.community.dashscope")
@Data
public class DashscopeAiModelConfig {

    private String apiKey;

    @Bean
    public OpenAiStreamingChatModel streamingChatModel() {

        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName("deepseek-r1")
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(5))
                .maxTokens(4096)
                .logRequests(true)
                .logResponses(true)
                .strictJsonSchema(true)
                .responseFormat("json_object")
                .build();
    }

    @Bean
    public OpenAiChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName("deepseek-r1") // 模型名称
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(5))
                .maxTokens(4096)
                .logRequests(true)
                .logResponses(true)
                .strictJsonSchema(true)
                .responseFormat("json_object")
                .build();
    }
}
