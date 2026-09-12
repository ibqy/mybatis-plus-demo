package com.xb.mybatisplus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建用户的接口入参 DTO。
 *
 * <p>record 很适合表达只承载数据的不可变请求对象。不要直接拿数据库实体接收外部输入，
 * 否则客户端可能修改 version、deleted、createdAt 等内部字段。</p>
 *
 * <p>这些 Bean Validation 注解负责尽早给客户端返回友好错误；数据库中的 NOT NULL、
 * UNIQUE 等约束仍然必须保留，它们才是数据完整性的最后防线。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
public record CreateUserRequest(
        // @NotBlank 同时拒绝 null、空字符串和只包含空格的字符串。
        @NotBlank(message = "姓名不能为空") String name,
        // @Min/@Max 不会拒绝 null，所以还要使用 @NotNull 与数据库 NOT NULL 约束保持一致。
        @NotNull(message = "年龄不能为空")
        @Min(value = 1, message = "年龄不能小于 1")
        @Max(value = 150, message = "年龄不能大于 150") Integer age,
        // @NotBlank 检查有没有值，@Email 检查值的格式，两者职责不同。
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email) {
}
