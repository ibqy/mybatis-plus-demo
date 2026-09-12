# 第 4 节：通用 CRUD

> 作者：xb  
> 日期：2026-08-12

[上一节](03-实体映射.md) · [返回目录](README.md)

## 1. 最小 Mapper

[UserMapper](../src/main/java/com/xb/mybatisplus/mapper/UserMapper.java#L19) 只需继承：

```java
public interface UserMapper extends BaseMapper<User> {
}
```

它立即拥有 `insert`、`selectById`、`selectList`、`updateById`、`deleteById` 等方法。泛型 `User` 告诉框架操作哪种实体、哪张表。

## 2. 查询

Controller 中的[列表和主键查询](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L58)：

```java
userService.list();
userService.getById(id);
```

底层大致对应：

```sql
SELECT id, name, age, email, status, created_at, updated_at, version, deleted
FROM demo_user
WHERE deleted = 0;

SELECT ... FROM demo_user WHERE id = ? AND deleted = 0;
```

底层 `getById` 在记录不存在时会返回 `null`。本项目通过 `getRequiredById` 把这种情况转换为业务异常，再由全局异常处理器返回 HTTP 404，避免出现含义模糊的 `200 + null`。

## 3. 新增

查看完整实现：[UserServiceImpl.create](../src/main/java/com/xb/mybatisplus/service/impl/UserServiceImpl.java#L27)。

步骤是 DTO → Entity → `save(user)`。执行成功后，自增 id 和自动填充时间会回写到对象。

```powershell
curl.exe -X POST http://localhost:8080/api/demo2/users `
  -H "Content-Type: application/json" `
  -d '{"name":"新学员","age":22,"email":"student@example.com"}'
```

## 4. 更新

查看[乐观锁更新实现](../src/main/java/com/xb/mybatisplus/service/impl/UserServiceImpl.java#L66)。`updateById` 只会用主键定位记录；默认字段策略通常不会把 `null` 更新进数据库。

初学者常犯的错误是先构造一个缺少 id 的实体再调用 `updateById`。没有主键，框架不知道更新哪一行。

## 5. 删除

查看[删除接口](../src/main/java/com/xb/mybatisplus/controller/UserLessonController.java#L123)。虽然代码调用 `removeById`，由于实体使用 `@TableLogic`，最终是逻辑删除的 UPDATE。

## 6. Mapper 和 Service 两套 API 如何选

- Mapper：贴近 SQL，一次数据库操作；返回受影响行数或数据。
- Service：在 Mapper 上提供批量操作、事务编排和更接近业务的复用点。

Controller 优先依赖自己的业务 Service，避免以后业务规则散落在各接口。简单教学查询仍可展示 Mapper 的直接调用。

## 7. 返回值必须检查

`insert/update/delete` 返回的受影响行数，以及 Service 的布尔值，都不应盲目忽略。0 可能表示记录不存在、条件不匹配或乐观锁冲突。本项目更新失败时抛出明确异常：[冲突处理](../src/main/java/com/xb/mybatisplus/service/impl/UserServiceImpl.java#L80)。

## 8. 动手实验

1. 调用新增接口，再用返回的 id 查询。
2. 删除该 id，再次查询，观察逻辑删除效果和 HTTP 404。
3. 第二次删除同一个 id，观察“写操作影响 0 行”如何转换成 404。
4. 尝试插入重复邮箱，阅读数据库异常中的唯一索引名称。

---

[返回课件目录](README.md) · [下一节：Lambda 单表查询](05-Lambda单表查询.md)
