-- 作者：xb；日期：2026-08-12

CREATE TABLE IF NOT EXISTS demo_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    age INT NOT NULL COMMENT '年龄',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_demo_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
