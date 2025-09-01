package com.zerocode.ratelimiter.aop;

import cn.hutool.core.util.StrUtil;
import com.zerocode.common.ThrowUtil;
import com.zerocode.domain.entity.User;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
import com.zerocode.ratelimiter.annotation.RateLimit;
import com.zerocode.ratelimiter.model.RateLimiterTypeEnum;
import com.zerocode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 限流切面
 */
@Aspect
@Component
public class RateLimitAOP {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;

    /**
     * 限流操作
     *
     * @param joinPoint
     * @param rateLimit
     */
    @Before("@annotation(rateLimit)")
    public void authCheck(JoinPoint joinPoint, RateLimit rateLimit) {
        // 1.生成限流key
        String rateLimitKey = generateRateLimitKey(joinPoint, rateLimit);
        // 2.获取限流注解参数
        int rateLimitCount = rateLimit.rateLimitCount();
        int timeWindowSeconds = rateLimit.timeWindowSeconds();
        String errorMsg = rateLimit.errorMsg();
        // 3.根据参数设置限流
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitKey);
        rateLimiter.trySetRate(RateType.OVERALL, rateLimitCount, timeWindowSeconds, RateIntervalUnit.SECONDS);
        rateLimiter.expire(Duration.ofHours(1));
        // 4.尝试获取令牌
        ThrowUtil.throwIf(!rateLimiter.tryAcquire(), ErrorCode.OPERATION_ERROR, errorMsg);
    }

    /**
     * 生成限流key
     *
     * @param rateLimit
     */
    private String generateRateLimitKey(JoinPoint joinPoint, RateLimit rateLimit) {
        // 1.获取限流类型 type 和 key 前缀，构建 key
        String keyPrefix = rateLimit.key();
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");
        if (StrUtil.isNotBlank(keyPrefix)) {
            keyBuilder.append(keyPrefix).append(":");
        }
        // 2.根据限流类型生成限流key
        RateLimiterTypeEnum rateLimiterType = rateLimit.type();
        switch (rateLimiterType) {
            case API:
                // 获取方法名
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                String methodName = methodSignature.getMethod().getName();
                // 构建key - api:方法名
                keyBuilder.append("api:").append(methodName);
                break;
            case USER:
                // 获取登录用户
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    User loginUser = userService.getLoginUser(request);
                    // 构建key - user:用户id
                    keyBuilder.append("user:").append(loginUser.getId());
                } else {
                    // 降级为 IP 限流
                    String ip = getClientIP();
                    keyBuilder.append("ip:").append(ip);
                }
                break;
            case IP:
                String ip = getClientIP();
                keyBuilder.append("ip:").append(ip);
                break;
            default:
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

}
