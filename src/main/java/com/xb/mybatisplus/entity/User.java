package com.xb.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

/**
 * 数据库实体：一个对象对应 demo_user 表的一行。
 * 本项目不使用 Lombok，便于初学者直接看到 JavaBean 的完整结构。
 *
 * <p>实体类负责描述“数据库怎样保存用户”，不等于 HTTP 接口允许客户端提交的内容。
 * 所以创建和更新接口另外使用 DTO，避免客户端直接控制 deleted、version 等内部字段。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
// 类名 User 与表名 demo_user 不一致，所以用 @TableName 明确指定真实表名。
@TableName("demo_user")
public class User {

    /** AUTO 表示使用 MySQL 自增主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户姓名。字段名与列名相同，不需要额外添加 @TableField。 */
    private String name;

    /** 使用包装类型 Integer 而不是 int，使对象能够表达“尚未赋值”的 null。 */
    @TableField(value = "age", exist = true)
    private Integer age;

    /** 邮箱唯一性最终由数据库唯一索引 uk_demo_user_email 保证。 */
    private String email;

    /** 业务状态：教学约定 1=启用，0=停用；它和 deleted 不是同一个概念。 */
    private Integer status;

    /** INSERT 表示新增时由 MetaObjectHandler 自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** INSERT_UPDATE 表示新增和更新时都会自动填充。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号，更新成功后 MyBatis-Plus 会自动加 1。
     * 客户端更新时必须携带刚刚查询到的版本号，旧版本更新会影响 0 行。
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除：调用 delete 后执行 UPDATE deleted=1，记录本身仍在数据库中。
     * BaseMapper 的普通查询会自动增加 deleted=0，但自定义 XML SQL 需要手动处理。
     */
    @TableLogic
    private Integer deleted;

    // 以下是标准 JavaBean 访问器。MyBatis 和 Jackson 都会使用这些属性访问规则。
    // 教学项目故意不使用 Lombok，方便观察一个完整实体类的真实结构。
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
