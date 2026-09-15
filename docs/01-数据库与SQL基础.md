# 第 1 节：数据库与 SQL 基础

> 作者：xb  
> 日期：2026-08-12

[返回课件目录](00-课程目录.md)

## 1. 学习目标

学完本节，你应该能读懂 `demo_user` 的建表语句，手写增删改查 SQL，并理解 SQL 与 Java 对象之间为什么需要“映射”。

## 2. 从一张表开始

关系型数据库可以先理解为许多有规则的二维表：

| id | name | age | email | status |
|---:|---|---:|---|---:|
| 1 | 张三 | 20 | zhangsan@example.com | 1 |

- 表（table）：同一类数据的集合，例如用户表。
- 行（row）：一个具体对象，例如张三。
- 列（column）：对象的一项属性，例如年龄。
- 主键（primary key）：唯一标识一行，不能重复。
- 索引（index）：类似书的目录，提高查找速度，但会增加写入成本和存储。
- 约束（constraint）：数据库层面的规则，如 `NOT NULL`、`UNIQUE`。

查看项目真实建表脚本：[schema.sql](../src/main/resources/schema.sql#L3)。

这里的 `AUTO_INCREMENT` 表示插入时不传 id，MySQL 自动产生下一个 id。`UNIQUE` 保证邮箱不重复；这类关键规则应由数据库兜底，不能只依赖 Java 校验。

## 3. CRUD 是什么

CRUD 是四类数据操作的缩写：Create、Read、Update、Delete。

### 3.1 新增 INSERT

```sql
INSERT INTO demo_user(name, age, email, status, created_at, updated_at, version, deleted)
VALUES ('小白', 18, 'xiaobai@example.com', 1, NOW(), NOW(), 0, 0);
```

### 3.2 查询 SELECT

SQL 推荐按这个顺序阅读：

```sql
SELECT id, name, age                 -- 要哪些列
FROM demo_user                       -- 从哪张表
WHERE deleted = 0 AND age >= 18      -- 过滤哪些行
ORDER BY age DESC                    -- 怎样排序
LIMIT 0, 5;                          -- 从第 0 条起取 5 条
```

逻辑执行顺序并不完全等于书写顺序。入门阶段先记住：`FROM → WHERE → SELECT → ORDER BY → LIMIT`。

### 3.3 更新 UPDATE

```sql
UPDATE demo_user
SET age = 19, updated_at = NOW()
WHERE id = 1;
```

### 3.4 删除 DELETE

物理删除会真正移除记录：

```sql
DELETE FROM demo_user WHERE id = 1;
```

本项目使用逻辑删除，实际执行的是：

```sql
UPDATE demo_user SET deleted = 1 WHERE id = 1 AND deleted = 0;
```

## 4. WHERE 是安全边界

缺少 `WHERE` 的更新会修改整张表：

```sql
-- 危险：所有用户都会被禁用
UPDATE demo_user SET status = 0;
```

本项目使用 `BlockAttackInnerInterceptor` 阻止无条件全表更新/删除。查看[防全表操作配置](../src/main/java/com/xb/mybatisplus/config/MybatisPlusConfig.java#L32)。数据库权限、备份和代码审查仍然不可省略。

## 5. SQL 参数为什么不能直接拼字符串

错误思路：

```java
"SELECT * FROM demo_user WHERE name = '" + userInput + "'"
```

如果输入包含恶意 SQL，可能产生 SQL 注入。MyBatis 的 `#{name}` 使用预编译参数，SQL 结构和数据分开传给数据库：

```sql
SELECT * FROM demo_user WHERE name = ?
```

项目 XML 的安全参数示例：[UserMapper.xml](../src/main/resources/mapper/UserMapper.xml#L20)。除非完全理解风险，不要使用 `${}` 接收外部输入。

## 6. 表和 Java 对象的差异

数据库列常用下划线：`created_at`；Java 属性常用驼峰：`createdAt`。MyBatis 的映射工作，就是把查询结果的一行转换为一个 `User` 对象。本项目开启了下划线转驼峰：[application.yml](../src/main/resources/application.yml#L24)。

| 数据库 | Java |
|---|---|
| `BIGINT` | `Long` |
| `INT` | `Integer` |
| `VARCHAR` | `String` |
| `DATETIME` | `LocalDateTime` |
| `created_at` | `createdAt` |

## 7. 动手实验

练习 SQL 位于 [lesson-queries.sql](../src/main/resources/lesson-queries.sql)。先猜结果，再逐条执行：

1. 查询所有未删除用户。
2. 查询年龄在 18～30 岁之间的启用用户。
3. 按年龄倒序取前两名。
4. 修改一个用户并观察 `updated_at`。
5. 把物理删除改写为逻辑删除。

## 8. 本节检查

- `WHERE` 和 `HAVING` 的过滤对象有什么不同？（本项目暂未使用分组，先知道 `HAVING` 用于分组后的结果。）
- 为什么邮箱既做接口校验，又做数据库唯一约束？
- `LIMIT 5, 10` 中两个数字分别是什么？

---

[返回课件目录](00-课程目录.md) · [下一节：项目架构与启动](02-项目架构与启动.md)
