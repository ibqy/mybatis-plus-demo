# 第 8 节：自定义 XML SQL

> 作者：xb  
> 日期：2026-08-12

[上一节](07-高级特性.md) · [返回目录](00-课程目录.md)

## 1. 什么时候离开通用 CRUD

BaseMapper 适合单表常见操作。遇到复杂关联、聚合报表、数据库特有函数或需要精确控制 SQL 时，应该编写清晰的自定义 SQL，而不是强行堆叠 Wrapper。

项目接口声明：[UserMapper.findActiveAdults](../src/main/java/com/xb/mybatisplus/mapper/UserMapper.java#L27)。XML 实现：[UserMapper.xml](../src/main/resources/mapper/UserMapper.xml#L13)。

## 2. namespace 与 id 如何绑定

```xml

<mapper namespace="com.xb.mybatisplus.mapper.UserMapper">
    <select id="findActiveAdults">...</select>
</mapper>
```

`namespace + id` 必须等于 Mapper 接口全限定名 + 方法名。参数 `@Param("minAge")` 对应 XML 的 `#{minAge}`。

## 3. `#{}` 与 `${}`

- `#{value}`：变成 JDBC `?` 参数，默认安全，应优先使用。
- `${value}`：直接文本替换，可能 SQL 注入，只适合经过严格白名单控制的 SQL 结构片段。

排序字段不能直接接受 `${sort}`。正确方式是在 Java 中把有限的外部选项映射为固定列，或者在 XML 用 `<choose>` 白名单。

## 4. resultType 与 resultMap

本项目列名可按驼峰自动映射，所以使用 `resultType="...User"`。复杂映射可使用 `resultMap`：

```xml
<resultMap id="userMap" type="com.xb...User">
    <id column="id" property="id"/>
    <result column="created_at" property="createdAt"/>
</resultMap>
```

联表的一对一、一对多也可用 resultMap，但要警惕 N+1 查询：查询一批用户后又为每个用户单独查询一次明细，会让 SQL 数随数据量增长。

## 5. 动态 SQL

MyBatis XML 支持 `<if>`、`<where>`、`<foreach>`、`<choose>`：

```xml
<where>
    deleted = 0
    <if test="minAge != null">AND age &gt;= #{minAge}</if>
</where>
```

XML 中 `<` 是特殊字符，所以小于号通常写成 `&lt;`，或使用 CDATA。

## 6. XML 常见错误定位

- `Invalid bound statement`：namespace、id、XML 扫描路径不匹配。
- `Parameter 'x' not found`：`@Param` 名和 XML 引用不一致。
- 字段全是 null：列名与属性映射不一致。
- 逻辑删除数据被查出：手写 SQL 忘记 `deleted=0`。

XML 扫描位置配置见 [application.yml](../src/main/resources/application.yml#L21)。

## 7. 动手实验

新增 `findByEmailDomain(String domain)`：查询未删除、启用且邮箱以指定域结尾的用户。要求使用 `#{}`，并为它增加集成测试。

---

[返回课件目录](00-课程目录.md) · [下一节：MyBatis-Plus 简单实现原理](09-MyBatisPlus实现原理.md)
