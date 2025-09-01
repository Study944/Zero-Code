package com.zerocode.ratelimiter.model;

/**
 * 限流类型枚举
 */
public enum RateLimiterTypeEnum {
    // 接口限流
    API,
    // 用户限流
    USER,
    // IP 限流
    IP;
}
