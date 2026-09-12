package com.xb.mybatisplus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 更新用户的接口入参 DTO。
 * version 必须由最新查询结果带回，用来判断“读取之后数据是否被别人修改过”。
 *
 * @author xb
 * @since 2026-08-12
 */
public record UpdateUserRequest(
        @NotBlank(message = "姓名不能为空") String name,
        @NotNull(message = "年龄不能为空") @Min(1) @Max(150) Integer age,
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
        // 不允许缺少 version，否则无法进行可靠的乐观锁并发检查。
        @NotNull(message = "version 不能为空") Integer version) {
}
