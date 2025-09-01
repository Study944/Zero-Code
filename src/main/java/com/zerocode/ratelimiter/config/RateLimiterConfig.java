package com.zerocode.ratelimiter.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端配置类
 */
@Configuration
public class RateLimiterConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer() // 单机模式
                .setAddress("redis://" + host + ":" + port) // Redis 服务器地址
                .setDatabase(1) // 数据库索引
                .setConnectionPoolSize(10) // 连接池大小
                .setConnectTimeout(5000) // 连接超时时间
                .setConnectionMinimumIdleSize(10) // 最小空闲连接数
                .setRetryAttempts(3) // 重试次数
                .setTimeout(5000);// 命令执行超时时间
        if (password != null&& !password.isEmpty()){
            singleServerConfig.setPassword(password);
        }
        return Redisson.create(config);
    }
}
