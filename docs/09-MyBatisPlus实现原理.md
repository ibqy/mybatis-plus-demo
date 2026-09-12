# 第 9 节：MyBatis-Plus 简单实现原理

> 作者：xb  
> 日期：2026-08-12

[上一节](08-自定义XML-SQL.md) · [返回目录](README.md)

> 这一课不要求背源码类名，目标是建立正确心智模型：MyBatis-Plus 没有绕过 MyBatis 和 JDBC，它主要在启动期注册通用 SQL、在运行期构造参数并通过插件改写 SQL。

## 1. 为什么 Mapper 接口没有实现类也能运行

启动时 `@MapperScan` 扫描 [UserMapper](../src/main/java/com/xb/mybatisplus/mapper/UserMapper.java#L19)，MyBatis 为接口创建 JDK 动态代理。Spring 容器中注入的 `UserMapper` 实际是代理对象。

调用：

```java
userMapper.selectById(1L);
```

不是执行某个手写 Java 实现，而是代理根据“接口名 + 方法名”找到一个 `MappedStatement`，再交给 MyBatis Executor 执行。

## 2. MappedStatement 是什么

可以把它理解为一条已注册 SQL 的完整说明书，包含：

- 唯一 id；
- SQL 来源和动态 SQL 节点；
- 参数映射；
- 结果映射；
- SQL 类型（SELECT/INSERT/UPDATE/DELETE）；
- 缓存等配置。

XML 中 `UserMapper.findActiveAdults` 会在解析 XML 时注册。BaseMapper 的通用方法则由 MyBatis-Plus 在启动期注册。

## 3. “SQL 注入器”不是安全漏洞

MyBatis-Plus 源码中的 SQL Injector 指“向 MyBatis Configuration 注入通用 MappedStatement”。它和攻击领域的 SQL Injection 不是一回事。

概念流程：

```text
发现 UserMapper extends BaseMapper<User>
 → 从 User 泛型读取 @TableName、@TableId、字段信息
 → 为 selectById/insert/updateById 等方法构建 SQL 模板
 → 注册为 MappedStatement
 → 运行时代理可按方法名找到并执行
```

因此，BaseMapper 能工作依赖实体元数据正确；表名、主键或字段映射错误会直接反映到生成 SQL。

## 4. Wrapper 如何变成 WHERE

`LambdaQueryWrapper` 内部记录条件片段、参数名和值。`User::getAge` 会通过 Lambda 元信息解析为实体属性，再通过表元数据得到数据库列 `age`。

最终 SQL 仍使用参数占位符，不是把普通值直接拼进去。Wrapper 是 SQL 构造器，不是业务规则层，也不会判断你的条件是否符合需求。

## 5. 执行器与 JDBC

简化后的运行链：

```text
MapperProxy
 → SqlSessionTemplate（接入 Spring 事务）
 → Executor
 → StatementHandler
 → ParameterHandler 把 Java 值绑定到 ?
 → JDBC PreparedStatement
 → MySQL 执行
 → ResultSetHandler 映射为 User
```

TypeHandler 负责 Java 类型与 JDBC 类型转换，例如 `LocalDateTime` 与 `TIMESTAMP/DATETIME`。

## 6. 插件如何改写 SQL

[MybatisPlusInterceptor](../src/main/java/com/xb/mybatisplus/config/MybatisPlusConfig.java#L24) 接入 MyBatis 插件机制，内部插件依次处理将要执行的 SQL：

- 乐观锁增加 version 更新与条件；
- 防攻击插件分析 UPDATE/DELETE 是否缺少条件；
- 分页插件解析 SELECT，生成 COUNT，并添加 LIMIT。

这解释了为何升级到 MyBatis-Plus 3.5.9+ 后分页要显式引入 SQL 解析器依赖：[pom.xml](../pom.xml#L36)。

## 7. Spring 事务如何接上 MyBatis

Spring 通过代理拦截 `@Transactional` 方法，把同一线程内的数据访问绑定到同一个事务资源。`SqlSessionTemplate` 负责安全地获取/释放 SqlSession，并参与 Spring 事务同步。

这也是事务常要求从 Spring Bean 外部调用 public 方法的原因：只有经过代理，拦截器才有机会开启事务。

## 8. MyBatis-Plus 做了什么、没做什么

它做了：减少单表重复 CRUD、提供类型相对安全的条件构造、常用插件和生成工具。

它没做：设计数据模型、替你写业务规则、自动保证索引合理、消除 SQL 性能问题、自动让手写 SQL 遵守逻辑删除和权限规则。

成熟开发者仍需阅读最终 SQL、执行计划和数据库约束。

## 9. 自我讲解练习

请不用课件，向同伴讲清楚：

1. `UserMapper` 为什么可以注入？
2. `selectById` 的 SQL 在哪里来的？
3. Wrapper 如何避免硬编码列名？
4. 分页为何通常执行两条 SQL？
5. `@Transactional` 为什么可能因内部调用失效？

---

[返回课件目录](README.md) · [下一节：代码生成器、测试与排错](10-代码生成器测试与排错.md)
