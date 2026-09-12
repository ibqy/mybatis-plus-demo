package com.xb.mybatisplus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件集中配置。
 *
 * <p>这些插件不会替代数据库执行 SQL，而是在 SQL 交给 JDBC 之前进行检查或改写。
 * 插件按照添加顺序执行，因此顺序有意义；分页通常放在其他 SQL 改写插件之后。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 外层拦截器相当于插件容器，多个 InnerInterceptor 共用这一入口。
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 乐观锁：识别实体上的 @Version，更新时拼接旧版本条件并让版本号加 1。
        // 例如：UPDATE ... SET version = 2 WHERE id = 1 AND version = 1。
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防全表更新/删除：UPDATE/DELETE 忘记写 WHERE 时直接抛出异常。
        // 这是一道教学安全护栏，但不能替代数据库权限、备份和代码审查。
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 指定 DbType.MYSQL 后，分页插件会按照 MySQL 方言生成 LIMIT。
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 即使调用方传入更大的 size，单页最多也只允许查询 100 条。
        pagination.setMaxLimit(100L);
        interceptor.addInnerInterceptor(pagination);

        // 把配置完成的对象交给 Spring；后续 MyBatis 自动配置会使用这个 Bean。
        return interceptor;
    }
}
