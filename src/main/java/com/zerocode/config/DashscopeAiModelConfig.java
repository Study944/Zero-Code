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

    private String modelName = "deepseek-r1-0528";

    @Bean
    public OpenAiStreamingChatModel dashscopeStreamingChatModel() {

        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName(modelName)
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(5))
                .maxTokens(8192)
                .logRequests(true)
                .logResponses(true)
                .strictJsonSchema(true)
//                .responseFormat("json_object")
                .build();
    }

    @Bean
    public OpenAiChatModel dashscopeChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName(modelName) // 模型名称
                .apiKey(apiKey)
                .timeout(java.time.Duration.ofMinutes(5))
                .maxTokens(8192)
                .logRequests(true)
                .logResponses(true)
                .strictJsonSchema(true)
//                .responseFormat("json_object")
                .build();
    }
}
