package com.xb.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xb.mybatisplus.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 继承 BaseMapper 后自动拥有 insert、selectById、updateById、deleteById 等通用方法。
 * findActiveAdults 演示通用 CRUD 不够用时，如何在 XML 中编写自定义 SQL。
 *
 * <p>这个接口不需要编写实现类。应用启动时，MyBatis 会为它创建动态代理；
 * 方法被调用后，代理根据“接口全限定名 + 方法名”寻找对应的 MappedStatement。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询达到指定年龄的启用用户。
     *
     * @param minAge 最小年龄；@Param 把 Java 参数命名为 minAge，供 XML 使用 #{minAge} 引用
     * @return 满足条件且未被逻辑删除的用户列表
     */
    List<User> findActiveAdults(@Param("minAge") int minAge);

    /**
     * 根据可选条件动态查询用户（演示 <where> + <if> 动态 SQL）。
     * 所有参数均可选，内部通过 <if test="..."> 判断是否拼接条件。
     *
     * @param name   用户姓名（模糊匹配），为 null 或空串时忽略此条件
     * @param minAge 最小年龄，为 null 时忽略
     * @param maxAge 最大年龄，为 null 时忽略
     * @param status 业务状态，为 null 时忽略
     * @return 满足所有传入条件的用户列表
     */
    List<User> findByConditions(@Param("name") String name,
                                @Param("minAge") Integer minAge,
                                @Param("maxAge") Integer maxAge,
                                @Param("status") Integer status);

    /**
     * 根据 ID 集合批量查询用户（演示 <foreach> 集合遍历）。
     *
     * @param idList 主键 ID 列表，不能为空
     * @return 匹配的用户列表
     */
    List<User> findByIds(@Param("idList") List<Long> idList);

    /**
     * 查询未删除用户并按指定字段排序（演示 <choose> 白名单防御式排序）。
     * sortBy 和 sortDir 在 XML 中通过 <choose> 白名单验证，
     * 即使传入非法值也不会导致 SQL 注入或异常。
     *
     * @param sortBy  排序列名：支持 age / name / created_at，其他值默认 id
     * @param sortDir 排序方向：desc 为降序，其他值默认升序 asc
     * @return 排序后的用户列表
     */
    List<User> findUsersSorted(@Param("sortBy") String sortBy,
                               @Param("sortDir") String sortDir);
}
