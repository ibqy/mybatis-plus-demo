package com.xb.mybatisplus.exception;

/**
 * 表示客户端请求的业务资源不存在。
 *
 * <p>单独定义异常类型后，全局异常处理器可以精确地把它转换为 HTTP 404，
 * 而不会把所有 IllegalStateException 都误判为“资源不存在”。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
