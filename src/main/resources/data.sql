-- 作者：xb；日期：2026-08-12

-- 固定主键并使用 ON DUPLICATE KEY UPDATE，让示例数据可重复初始化。
INSERT INTO demo_user (id, name, age, email, status, created_at, updated_at, version, deleted)
VALUES (1, '张三', 20, 'zhangsan@example.com', 1, NOW(), NOW(), 0, 0),
       (2, '李四', 28, 'lisi@example.com', 1, NOW(), NOW(), 0, 0),
       (3, '王小明', 16, 'xiaoming@example.com', 1, NOW(), NOW(), 0, 0),
       (4, '赵六', 35, 'zhaoliu@example.com', 0, NOW(), NOW(), 0, 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);
