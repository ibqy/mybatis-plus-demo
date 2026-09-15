# 第 6 节：Service、事务与数据边界

> 作者：xb  
> 日期：2026-08-12

[上一节](05-Lambda单表查询.md) · [返回目录](00-课程目录.md)

## 1. Service 表达业务动作

[UserService](../src/main/java/com/xb/mybatisplus/service/UserService.java#L17) 不只是重复 Mapper 方法，还定义了 `create` 和 `updateWithOptimisticLock` 这类业务语义。

`ServiceImpl<UserMapper, User>` 提供通用实现，而本项目的 Service 负责：

- DTO 转 Entity；
- 设置业务默认值 `status=1`；
- 检查更新是否成功；
- 控制事务边界。

## 2. 什么是事务

事务把多条数据库操作视为一个整体，满足“全部成功或全部失败”。经典转账：

```text
账户 A -100
账户 B +100
```

如果第一条成功、第二条失败而没有事务，钱就凭空消失。`@Transactional` 会在方法开始前取得连接并开启事务；正常返回时提交，符合回滚规则的异常抛出时回滚。

查看项目事务位置：[create](../src/main/java/com/xb/mybatisplus/service/impl/UserServiceImpl.java#L27)、[update](../src/main/java/com/xb/mybatisplus/service/impl/UserServiceImpl.java#L66)。

## 3. 常见事务误区

- `private` 方法或同类内部 `this.method()` 调用，通常绕过 Spring 代理，事务可能不生效。
- 捕获异常后不再抛出，Spring 会认为方法正常完成并提交。
- 默认主要对运行时异常回滚；需要根据业务明确 `rollbackFor`。
- 跨数据库、消息队列的事务不是一个简单注解就能解决。
- 事务时间应尽量短，不要在事务中做长时间网络调用。

## 4. DTO、Entity、VO

```text
DTO：客户端允许输入什么
Entity：数据库保存什么
VO：接口允许输出什么
```

本项目为保持入门代码紧凑，响应暂时直接返回 Entity；生产项目通常增加 VO，以避免泄露内部字段 `deleted`、`version`，也避免数据库结构变化直接破坏 API。

参数校验入口：[CreateUserRequest](../src/main/java/com/xb/mybatisplus/dto/CreateUserRequest.java#L21)和 Controller 的 [`@Valid`](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L75)。校验失败由[全局异常处理器](../src/main/java/com/xb/mybatisplus/controller/GlobalExceptionHandler.java#L26)转换成 JSON。

## 5. HTTP 状态码也是接口设计

- `200 OK`：查询或更新成功。
- `201 Created`：创建成功，本项目新增接口使用它。
- `204 No Content`：删除成功且无响应体。
- `400 Bad Request`：参数格式或校验失败。
- `404 Not Found`：资源不存在。
- `409 Conflict`：乐观锁等状态冲突。

## 6. 动手实验

1. 新建 `UserResponse`，只输出 id、name、age、email 和时间。
2. 用一个私有转换方法把 `User` 转为 `UserResponse`。
3. 设计“批量创建两个用户，第二个邮箱重复”的实验，验证事务是否回滚第一个插入。

---

[返回课件目录](00-课程目录.md) · [下一节：高级特性](07-高级特性.md)
