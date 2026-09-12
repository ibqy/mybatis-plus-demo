-- 作者：xb；日期：2026-08-12
-- 第 1 节配套 SQL。建议在 mybatis_plus_demo 教学库执行。

-- 1. 查看表结构与数据。
DESCRIBE demo_user;
SELECT * FROM demo_user;

-- 2. 投影 + 过滤：只查询需要的列。
SELECT id, name, age, email
FROM demo_user
WHERE deleted = 0 AND status = 1;

-- 3. 多条件、区间和排序。
SELECT id, name, age
FROM demo_user
WHERE deleted = 0
  AND status = 1
  AND age BETWEEN 18 AND 30
ORDER BY age DESC;

-- 4. 模糊查询。% 表示任意长度字符，_ 表示单个字符。
SELECT id, name
FROM demo_user
WHERE name LIKE '%小%';

-- 5. 分页：第一页每页 2 条。第二页把 offset 改为 2。
SELECT id, name, age
FROM demo_user
WHERE deleted = 0
ORDER BY id ASC
LIMIT 0, 2;

-- 6. 聚合：统计未删除用户数和平均年龄。
SELECT COUNT(*) AS user_count, AVG(age) AS average_age
FROM demo_user
WHERE deleted = 0;

-- 7. 更新前先用相同 WHERE 执行 SELECT，确认目标行。
SELECT * FROM demo_user WHERE id = 1 AND deleted = 0;
UPDATE demo_user
SET age = age + 1, updated_at = NOW()
WHERE id = 1 AND deleted = 0;

-- 8. 逻辑删除及恢复。
UPDATE demo_user SET deleted = 1 WHERE id = 3 AND deleted = 0;
UPDATE demo_user SET deleted = 0 WHERE id = 3 AND deleted = 1;

-- 9. 乐观锁的核心 SQL：只有旧版本仍为 0 才能更新成功。
UPDATE demo_user
SET name = '张三（新）', version = version + 1, updated_at = NOW()
WHERE id = 1 AND version = 0 AND deleted = 0;
