package com.zerocode.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedisChatMemoryStore（Redis存储AI对话历史）配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {
    // 端口地址
    private String host;
    // 端口号
    private int port;
    // key默认超时时间
    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .ttl(ttl)
                .build();
    }
}
