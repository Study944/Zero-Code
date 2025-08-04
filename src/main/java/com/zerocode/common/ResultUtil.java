package com.zerocode.common;

import com.zerocode.exception.ErrorCode;

/**
 * 响应工具类
 */
public class ResultUtil {
    /**
     * 成功
     * @param data
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS, data);
    }

    /**
     * 失败
     * @param errorCode
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 错误
     * @param code, data
     * @param <T>
     */
    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(code, message);
    }
}
