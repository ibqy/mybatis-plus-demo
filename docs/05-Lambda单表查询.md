# 第 5 节：Lambda 单表查询（重点）

> 作者：xb  
> 日期：2026-08-12

[上一节](04-通用CRUD.md) · [返回课件目录](README.md) · [查看完整代码](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L23)

> 本节是整堂课的重点，把Lambda 单表查询最常用的写法集中在一张课件中，方便课堂演示和课后复习。

## 本节示例导航

下面每个示例都可以直接运行，并能跳转到具体代码行：

| 业务场景 | HTTP 接口 | Lambda 查询代码 | 自动测试 |
|---|---|---|---|
| 根据唯一邮箱查一条 | [Controller 第 94 行](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L94) | [findOneByEmail 第 37 行](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L37) | [测试第 91 行](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L91) |
| 姓名、年龄、状态组合查询 | [Controller 第 85 行](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L85) | [search 第 49 行](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L49) | [测试第 96 行](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L96) |
| 根据多个 id 查询 | [Controller 第 100 行](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L100) | [findByIds 第 67 行](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L67) | [测试第 101 行](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L101) |
| 统计启用用户数量 | [Controller 第 106 行](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L106) | [countByStatus 第 80 行](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L80) | [测试第 105 行](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L105) |
| 按状态分页 | [Controller 第 112 行](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L112) | [page 第 86 行](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L86) | [测试第 108 行](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L108) |

本节返回字段来自 [User 实体第 24 行](../src/main/java/com/xb/mybatisplus/entity/User.java#L24)，练习数据来自 [local-data.sql](../src/main/resources/local-data.sql#L3)。

## 一、固定使用步骤

```java
// 1. 创建 User 实体对应的 Lambda 查询条件构造器
LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
        // 2. 添加查询条件
        .eq(User::getStatus, 1)
        // 3. 添加排序条件
        .orderByAsc(User::getAge);

// 4. 根据需要选择 BaseMapper 查询方法
List<User> users = userMapper.selectList(wrapper);
```

记忆口诀：**选方法、建 Wrapper、加条件、执行查询。**

## 二、快速选择查询方法

| 查询目标 | 推荐方法 | 示例 |
|---|---|---|
| 已知主键，查询一条 | `selectById(id)` | [主键查询示例](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L69) |
| 按唯一条件查询一条 | `selectOne(wrapper)` | [邮箱精确查询](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L37) |
| 按条件查询多条 | `selectList(wrapper)` | [姓名、年龄、状态组合查询](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L49) |
| 只统计数量 | `selectCount(wrapper)` | [统计启用用户数量](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L80) |
| 分页查询 | `selectPage(page, wrapper)` | [查询指定页](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L86) |

## 三、查一条：`eq + selectOne`

场景：登录或资料查询时，根据唯一邮箱 `zhangsan@example.com` 查找张三。

```java
User user = userMapper.selectOne(
        Wrappers.<User>lambdaQuery()
                // eq 表示“等于”，大致生成 email = ?
                .eq(User::getEmail, email)
);
```

- 适用于主键、唯一邮箱等最多返回一条数据的条件。
- 如果实际返回多条，`selectOne` 会抛出异常。
- 接口调用：`GET /api/demo3/users/by-email?email=zhangsan@example.com`
- 预期结果：返回张三这一条用户数据。
- 跳转代码：[接口入口](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L94) → [Lambda 查询实现](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L37) → [测试断言](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L91)。

## 四、查多条：可选条件 + `selectList`

场景：查询姓名包含“张”、年龄在 18～30 岁之间、状态为启用的用户。

```java
LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
        // boolean 为 false 时，不会把该条件加入 SQL
        .like(keyword != null && !keyword.isBlank(), User::getName, keyword)
        .ge(minAge != null, User::getAge, minAge)
        .le(maxAge != null, User::getAge, maxAge)
        .eq(status != null, User::getStatus, status)
        .orderByAsc(User::getAge)
        .orderByAsc(User::getId);

List<User> users = userMapper.selectList(wrapper);
```

大致生成：

```sql
SELECT ... FROM demo_user
WHERE deleted = 0
  AND name LIKE ?
  AND age >= ?
  AND age <= ?
  AND status = ?
ORDER BY age ASC, id ASC;
```

实际请求：

```text
GET /api/demo3/users/search?keyword=张&minAge=18&maxAge=30&status=1
```

使用项目初始化数据时，预期只返回张三。删掉 `maxAge` 参数后，`.le(false, ...)` 不会进入 SQL，这正是可选条件写法的作用。

跳转代码：[接口参数接收](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L85) → [like、ge、le、eq 实现](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L49) → [组合查询测试](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L96)。

## 五、按集合查询：`in + selectList`

场景：用户勾选了 id 为 1、2、3 的三条记录，需要一次查询这些用户。

```java
if (ids == null || ids.isEmpty()) {
    return List.of();
}

return userMapper.selectList(
        Wrappers.<User>lambdaQuery()
                .in(User::getId, ids)
                .orderByAsc(User::getId)
);
```

`in` 大致生成 `id IN (?, ?, ?)`。空集合要提前处理，防止条件被跳过后意外查询整张表。

- 接口调用：`GET /api/demo3/users/by-ids?ids=1,2,3`
- 预期结果：返回 id 位于 1、2、3 集合中的用户，并按 id 升序排列。
- 跳转代码：[接口入口](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L100) → [空集合保护和 in 查询](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L67) → [正常集合与空集合测试](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L101)。

## 六、统计数量：`selectCount`

场景：页面上只显示“启用用户共多少人”，不需要读取每个用户的完整资料。

```java
Long count = userMapper.selectCount(
        Wrappers.<User>lambdaQuery()
                .eq(User::getStatus, 1)
);
```

只需要数量时不要先 `selectList` 再调用 `size()`，应让数据库直接执行 `COUNT`。

- 接口调用：`GET /api/demo3/users/count?status=1`
- 返回示例：`3`，表示初始化数据中有 3 个启用且未逻辑删除的用户。
- 跳转代码：[接口入口](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L106) → [selectCount 实现](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L80) → [数量断言](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L105)。

## 七、分页查询：`selectPage`

场景：用户列表每页只展示 2 条，并且只查看启用用户。

```java
Page<User> page = userMapper.selectPage(
        Page.of(current, size),
        Wrappers.<User>lambdaQuery()
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreatedAt)
);
```

常用分页结果：

| 属性 | 含义 |
|---|---|
| `page.getRecords()` | 当前页数据 |
| `page.getTotal()` | 符合条件的总记录数 |
| `page.getCurrent()` | 当前页码 |
| `page.getSize()` | 每页数量 |
| `page.getPages()` | 总页数 |

实际请求：

```text
GET /api/demo4/users/page?current=1&size=2&status=1
```

返回结构示例：

```json
{
  "records": [
    {"id": 3, "name": "王小明", "age": 16, "status": 1},
    {"id": 2, "name": "李四", "age": 28, "status": 1}
  ],
  "total": 3,
  "size": 2,
  "current": 1,
  "pages": 2
}
```

跳转代码：[分页参数入口](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L112) → [Lambda 条件分页实现](../src/main/java/com/xb/mybatisplus/service/UserLambdaQueryService.java#L86) → [records 和 total 测试](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L108) → [分页插件配置](../src/main/java/com/xb/mybatisplus/config/MybatisPlusConfig.java#L21)。

## 八、常用 Lambda 条件对照

| MyBatis-Plus 写法 | 含义 | 大致 SQL |
|---|---|---|
| `.eq(User::getStatus, 1)` | 等于 | `status = ?` |
| `.ne(User::getStatus, 0)` | 不等于 | `status <> ?` |
| `.gt(User::getAge, 18)` | 大于 | `age > ?` |
| `.ge(User::getAge, 18)` | 大于等于 | `age >= ?` |
| `.lt(User::getAge, 60)` | 小于 | `age < ?` |
| `.le(User::getAge, 60)` | 小于等于 | `age <= ?` |
| `.between(User::getAge, 18, 30)` | 范围 | `age BETWEEN ? AND ?` |
| `.like(User::getName, "张")` | 模糊包含 | `name LIKE '%张%'` |
| `.likeLeft(User::getName, "三")` | 左侧模糊 | `name LIKE '%三'` |
| `.likeRight(User::getName, "张")` | 右侧模糊 | `name LIKE '张%'` |
| `.in(User::getId, ids)` | 属于集合 | `id IN (?, ?)` |
| `.isNull(User::getEmail)` | 字段为空 | `email IS NULL` |
| `.isNotNull(User::getEmail)` | 字段不为空 | `email IS NOT NULL` |
| `.orderByAsc(User::getAge)` | 升序 | `ORDER BY age ASC` |
| `.orderByDesc(User::getId)` | 降序 | `ORDER BY id DESC` |

## 九、AND、OR 和括号

```java
// status = 1 AND (age < 18 OR age > 60)
wrapper.eq(User::getStatus, 1)
       .and(w -> w.lt(User::getAge, 18)
                  .or()
                  .gt(User::getAge, 60));
```

内层 `w -> ...` 用于生成括号。复杂查询不要只看 Java 链式代码，还要查看控制台打印的实际 SQL。

## 十、为什么使用 `User::getName`

```java
// 不推荐：列名是字符串，字段重命名时不容易发现问题
wrapper.eq("name", keyword);

// 推荐：方法引用可以参与编译检查和 IDE 重构
wrapper.eq(User::getName, keyword);
```

MyBatis-Plus 的简单处理过程：

```text
User::getName
  → 解析为 User 的 name 属性
  → 根据实体映射找到 name 列
  → Wrapper 保存条件和值
  → BaseMapper 生成并执行参数化 SQL
```

## 十一、接口练习

```powershell
# 多条件查询
curl.exe "http://localhost:8080/api/demo3/users/search?keyword=张&minAge=18&maxAge=30&status=1"

# 根据唯一邮箱查一条
curl.exe "http://localhost:8080/api/demo3/users/by-email?email=zhangsan@example.com"

# 根据多个 id 查询
curl.exe "http://localhost:8080/api/demo3/users/by-ids?ids=1,2,3"

# 统计启用用户数量
curl.exe "http://localhost:8080/api/demo3/users/count?status=1"

# 带状态条件的分页查询
curl.exe "http://localhost:8080/api/demo4/users/page?current=1&size=2&status=1"
```

## 十二、新手避坑

- `selectOne` 只用于最多返回一条的条件。
- `selectList(null)` 可能查询整张表，使用前要确认数据规模。
- `in` 的集合为空时应提前处理。
- 可选参数优先使用条件方法的第一个 boolean 参数。
- 排序字段不要直接接收前端字符串并拼接到 SQL。
- Lambda Wrapper 负责构造条件，但不能代替数据库索引设计。
- 写完组合条件后，查看 SQL 并运行[Lambda 单表查询集成测试](../src/test/java/com/xb/mybatisplus/UserMapperIntegrationTest.java#L89)。

---

[返回课件目录](README.md) · [下一节：Service、事务与数据边界](06-Service事务与数据边界.md)
