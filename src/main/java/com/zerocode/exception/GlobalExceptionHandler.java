package com.zerocode.exception;

import com.zerocode.common.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 处理自定义业务异常
     * @param e
     * @return BaseResponse
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<String> businessExceptionHandler(BusinessException e) {
        log.error("businessException: {}", e.getMessage());
        return new BaseResponse<>(e.getCode(), e.getMessage());
    }

    /**
     * 系统内部异常
     * @param e
     * @return BaseResponse
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<String> exceptionHandler(Exception e) {
        log.error("exception: {}", e.getMessage());
        return new BaseResponse<>(ErrorCode.SYSTEM_ERROR);
    }
}
