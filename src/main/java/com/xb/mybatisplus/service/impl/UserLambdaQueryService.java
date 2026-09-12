package com.xb.mybatisplus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xb.mybatisplus.entity.User;
import com.xb.mybatisplus.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyBatis-Plus Lambda 单表查询教学服务。
 *
 * <p>本类故意集中展示最常用的单表查询写法。学习时建议按照
 * {@code selectOne -> selectList -> selectCount -> selectPage} 的顺序阅读，
 * 并观察每个 Wrapper 方法最终生成的 SQL。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
@Service
public class UserLambdaQueryService {

    private final UserMapper userMapper;

    public UserLambdaQueryService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据唯一邮箱查询一条记录。
     *
     * <p>{@code User::getEmail} 是 Lambda 方法引用，MyBatis-Plus 会把它解析为
     * 实体属性 email，再根据实体映射找到数据库列 email。</p>
     */
    public User findOneByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
                // eq 对应 SQL 中的“email = ?”。
                .eq(User::getEmail, email);

        // selectOne 适用于结果最多一条的场景；如果数据库返回多条，会抛出异常。
        return userMapper.selectOne(wrapper);
    }

    /**
     * 多条件组合查询。所有参数都可以不传，适合搜索表单。
     */
    public List<User> search(String keyword, Integer minAge, Integer maxAge, Integer status) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
                // 第一个 boolean 为 false 时，这个条件不会加入 SQL，可少写多个 if。
                .like(hasText(keyword), User::getName, keyword)
                // ge = greater than or equal，对应 age >= ?。
                .ge(minAge != null, User::getAge, minAge)
                // le = less than or equal，对应 age <= ?。
                .le(maxAge != null, User::getAge, maxAge)
                .eq(status != null, User::getStatus, status)
                // 排序增加 id 作为第二排序键，让相同年龄的结果顺序保持稳定。
                .orderByAsc(User::getAge)
                .orderByAsc(User::getId);

        // selectList 返回所有满足 Wrapper 条件的记录。
        return userMapper.selectList(wrapper);
    }

    /** 使用 IN 条件按一组主键查询。 */
    public List<User> findByIds(List<Long> ids) {
        // 空集合不能忽略：如果直接跳过 in 条件，可能意外变成无条件查询全表。
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return userMapper.selectList(Wrappers.<User>lambdaQuery()
                // in 对应“id IN (?, ?, ...)”。
                .in(User::getId, ids)
                .orderByAsc(User::getId));
    }

    /** 按状态统计记录数，不需要先查询全部数据再调用 Java 的 size()。 */
    public long countByStatus(Integer status) {
        return userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(status != null, User::getStatus, status));
    }

    /** Lambda 条件与 MyBatis-Plus 分页组合使用。 */
    public Page<User> page(long current, long size, Integer status) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreatedAt)
                .orderByDesc(User::getId);

        // 分页插件会改写 SQL，并把 records、total 等结果写回 Page 对象。
        return userMapper.selectPage(Page.of(current, size), wrapper);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
