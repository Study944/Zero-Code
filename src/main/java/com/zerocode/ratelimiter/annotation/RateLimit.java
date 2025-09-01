package com.zerocode.ratelimiter.annotation;

import com.zerocode.ratelimiter.model.RateLimiterTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Redis 存储的 key 前缀
     */
    String key() default "";

    /**
     * 令牌数量
     */
    int rateLimitCount() default 5;

    /**
     * 时间窗口，令牌重置的时间周期
     */
    int timeWindowSeconds() default 60;

    /**
     * 限流类型
     */
    RateLimiterTypeEnum type() default RateLimiterTypeEnum.USER;

    /**
     * 错误信息
     */
    String errorMsg() default "请求过于频繁，请稍后再试";
}
