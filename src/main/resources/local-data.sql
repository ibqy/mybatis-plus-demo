-- 作者：xb；日期：2026-08-12

INSERT INTO demo_user (id, name, age, email, status, created_at, updated_at, version, deleted)
VALUES (1, '张三', 20, 'zhangsan@example.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0),
       (2, '李四', 28, 'lisi@example.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0),
       (3, '王小明', 16, 'xiaoming@example.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0),
       (4, '赵六', 35, 'zhaoliu@example.com', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0);
