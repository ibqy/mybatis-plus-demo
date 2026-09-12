package com.xb.mybatisplus.config;

import com.xb.mybatisplus.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局 REST 异常处理器。
 *
 * <p>{@code @RestControllerAdvice} 会拦截所有 Controller 抛出的匹配异常，
 * 把它们统一转换成初学者容易理解的 JSON 和正确的 HTTP 状态码。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 资源不存在时返回 404，而不是返回 200 + null。 */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(ResourceNotFoundException exception) {
        return Map.of("message", exception.getMessage());
    }

    /**
     * 处理 Controller 方法参数校验，例如分页 current/size 上的 @Min、@Max。
     * 它与请求体 DTO 校验抛出的 MethodArgumentNotValidException 是两条不同异常链。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            // propertyPath 形如 page.current，能够说明是哪个方法的哪个参数不合法。
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return errors;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 客户端提交的数据不符合约束属于 400 Bad Request。
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        // LinkedHashMap 保留字段错误的加入顺序，让示例响应更稳定、便于阅读。
        Map<String, String> errors = new LinkedHashMap<>();

        // 一次请求可能有多个字段校验失败，因此逐项收集为“字段名 -> 错误消息”。
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

    @ExceptionHandler(IllegalStateException.class)
    // 资源存在但当前状态不允许操作（例如 version 过期），使用 409 Conflict。
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(IllegalStateException exception) {
        // 返回结构化 JSON：{"message":"..."}，而不是把异常堆栈暴露给客户端。
        return Map.of("message", exception.getMessage());
    }
}
