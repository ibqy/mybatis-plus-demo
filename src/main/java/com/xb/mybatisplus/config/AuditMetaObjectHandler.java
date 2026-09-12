package com.xb.mybatisplus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充处理器：统一维护创建时间和更新时间，业务代码无需重复赋值。
 *
 * <p>实体字段还必须通过 {@code @TableField(fill = ...)} 声明填充时机，
 * 声明和处理器缺少任何一端，自动填充都不会按预期工作。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 一次插入共用同一个 now，确保创建时间和更新时间完全一致。
        LocalDateTime now = LocalDateTime.now();

        // 参数中的字符串是 Java 属性名 createdAt，不是数据库列名 created_at。
        // strictInsertFill 默认不会粗暴覆盖调用方已经明确设置的非空值。
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时只刷新 updatedAt；createdAt 应一直保留最初创建记录的时间。
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
