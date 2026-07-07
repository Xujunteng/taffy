package com.taffy.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获 Controller 层抛出的异常，返回规范化的 Result 响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常（如用户名已存在、密码错误等）
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 参数校验异常（@Valid 场景，本项目使用手动校验，此方法做兜底）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 未预见异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return Result.error(500, "服务器内部错误，请稍后再试");
    }
}