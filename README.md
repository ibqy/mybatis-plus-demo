# Spring Boot + MyBatis-Plus 渐进式教学项目

> 作者：xb
> 日期：2026-08-12

这是一个可运行、可调接口、可观察 SQL 的新手教学工程。整堂教学课重点讲解 MyBatis-Plus Lambda 表达式和快速单表查询，并用 `demo1` 到 `demo6` 逐步补充 CRUD、分页、高级特性、XML 和代码生成器。

## 完整教学课件

请从 **[《MyBatis-Plus 从零到原理》教学课件目录](docs/00-课程目录.md)** 开始。整堂课分为多个小节，覆盖数据库和 SQL 基础、Spring Boot 分层、实体映射、CRUD、Wrapper、分页、事务、高级插件、自定义 XML、MyBatis-Plus 简单实现原理、代码生成器、测试排错与综合练习；每一节都可跳转到对应代码示例。

Lambda 是本项目的课堂重点，可直接学习 **[第 5 节：MyBatis-Plus Lambda 单表查询](docs/05-Lambda单表查询.md)**。

## 技术版本与环境

- Java 21
- Spring Boot 4.1.0
- MyBatis-Plus 3.5.17（Spring Boot 4 Starter）
- Maven Wrapper 3.9.16
- MySQL 8

不要使用机器上可能较旧的 `mvn`，Windows 请运行 `mvnw.cmd`，macOS/Linux 请运行 `./mvnw`。IDEA 的 Project SDK 也要选择 Java 21。

## 1. 启动项目

默认配置会连接题目指定的 MySQL，并尝试创建 `mybatis_plus_demo` 数据库、`demo_user` 表和四条示例数据：

```powershell
$env:JAVA_HOME='C:\Users\xb1\.jdks\ms-21.0.11'
.\mvnw.cmd spring-boot:run
```

如账号没有建库权限，请让管理员先执行：

```sql
CREATE DATABASE mybatis_plus_demo DEFAULT CHARACTER SET utf8mb4;
```

如果暂时不在公司内网，可显式启用内存数据库完成课程练习（默认配置仍是 MySQL）：

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

`local` 数据只存在于当前进程，停止应用后自动清空，适合反复练习。

生产实践中不要把密码写进配置文件。本教学项目支持环境变量覆盖：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。

## 2. 分节练习

启动后打开另一个终端执行：

```powershell
# demo1：通用列表、按主键查询
curl.exe http://localhost:8080/api/demo1/users
curl.exe http://localhost:8080/api/demo1/users/1

# demo2：新增；观察 id、createdAt、updatedAt 自动回填
curl.exe -X POST http://localhost:8080/api/demo2/users -H "Content-Type: application/json" -d '{"name":"新学员","age":22,"email":"student@example.com"}'

# demo3（重点）：Lambda 单表查询；条件参数均可选
curl.exe "http://localhost:8080/api/demo3/users/search?keyword=张&minAge=18&maxAge=30&status=1"
curl.exe "http://localhost:8080/api/demo3/users/by-email?email=zhangsan@example.com"
curl.exe "http://localhost:8080/api/demo3/users/by-ids?ids=1,2,3"
curl.exe "http://localhost:8080/api/demo3/users/count?status=1"

# demo4：分页；观察 records、total、pages
curl.exe "http://localhost:8080/api/demo4/users/page?current=1&size=2&status=1"

# demo5：把查询到的 version 原样带回，成功后 version 会加 1
curl.exe -X PUT http://localhost:8080/api/demo5/users/1 -H "Content-Type: application/json" -d '{"name":"张三（已更新）","age":21,"email":"zhangsan@example.com","version":0}'

# demo5：逻辑删除和 XML 自定义 SQL
curl.exe -X DELETE http://localhost:8080/api/demo5/users/3
curl.exe "http://localhost:8080/api/demo5/users/active-adults?minAge=18"
```

建议一边调用，一边看控制台输出的 SQL，然后依次阅读 `entity` → `mapper` → `service` → `controller`。

## 实现边界

### ✅ 已实现

| 功能 | 说明 | 教学价值 |
|------|------|---------|
| 基础 CRUD | BaseMapper 单表操作 | 理解 MyBatis-Plus 最基础能力 |
| Lambda 查询 | LambdaQueryWrapper 条件构造 | **课件重点**，类型安全的条件组合 |
| 分页查询 | Page 对象 + 分页插件 | 自动 COUNT + LIMIT 改写 |
| 乐观锁 | @Version + 插件 | 并发更新的数据一致性保障 |
| 逻辑删除 | @TableLogic | 数据软删除，可恢复 |
| 自动填充 | MetaObjectHandler | createdAt/updatedAt 自动维护 |
| 自定义 SQL | XML Mapper | 复杂查询仍需手写 SQL 时的方案 |
| 批量操作 | saveBatch + LambdaUpdateWrapper | 高效批量插入/更新 |
| 代码生成器 | MybatisPlusCodeGenerator | 快速生成 CRUD 代码 |

### 🎓 教学简化

| 场景 | 简化内容 | 生产环境建议 |
|------|---------|-------------|
| 多租户 | 未实现 | 使用 TenantLineInnerInterceptor |
| 动态表名 | 未实现 | 使用 DynamicTableNameInnerInterceptor |
| 读写分离 | 未实现 | 集成 ShardingSphere 或 MyBatis 路由 |
| 缓存 | 未实现 | 集成 Redis + @Cacheable |
| 分布式锁 | 未实现 | 使用 Redisson 或数据库锁 |
| 审计日志 | 仅时间字段 | 完整审计需记录操作人/IP/变更内容 |

### ❌ 未实现

- 多表关联查询（MyBatis-Plus 专注单表）
- 复杂报表 SQL（应使用 XML 或专门模块）
- 数据库迁移工具（建议 Flyway/Liquibase）
- 性能监控与慢 SQL 分析

## 测试覆盖

| 测试类 | 测试数 | 覆盖内容 |
|--------|--------|---------|
| `UserMapperIntegrationTest` | 4 | BaseMapper、XML 查询、分页、逻辑删除、Lambda 查询、乐观锁、自动填充 |
| `ServiceLayerIntegrationTest` | 4 | 批量创建、批量更新状态、批量逻辑删除、空列表安全处理 |
| **合计** | **8** | **全部通过** |

```bash
# 运行测试（使用 H2 内存数据库，不连接远程 MySQL）
mvn test
```

<br>

---

## 3. demo6：运行代码生成器

先正常启动一次，确保表已创建；停止应用后执行：

```powershell
.\mvnw.cmd exec:java "-Dexec.mainClass=generator.com.xb.mybatisplus.MybatisPlusCodeGenerator"
```

生成结果位于 `target/generated-code`，不会覆盖教学源码。重点阅读 `MybatisPlusCodeGenerator` 中的全局配置、包配置、策略配置和模板引擎配置。

## 4. 安全测试

```powershell
.\mvnw.cmd clean test
```

测试自动使用内存 H2 数据库，不连接也不修改远程 MySQL。测试覆盖 BaseMapper、XML 查询、分页和逻辑删除。

## 目录导读

```text
src/main/java/com/xb/mybatisplus
├── config       # 分页/乐观锁/防全表操作插件，字段自动填充
├── controller   # demo1～demo5 REST 教学入口
├── dto          # 参数校验与接口数据边界
├── entity       # 表映射、主键、乐观锁、逻辑删除注解
├── mapper       # BaseMapper 和自定义 SQL 接口
├── service      # IService/ServiceImpl 与事务
└── generator    # demo6 自动代码生成器
```

> 提醒：远程数据库属于共享资源时，不要在课堂中使用真实业务表。本项目所有对象均以 `demo_` 命名。

---

<p align="center">
  <a href="https://github.com/ibqy">🏠 回到 ibqy 主页</a> · <a href="https://ibqy.github.io">🌐 作品集</a> · <a href="https://gitee.com/baiqy/mybatis-plus-demo">🇨🇳 Gitee</a>
</p>
