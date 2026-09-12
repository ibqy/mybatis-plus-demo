package com.xb.mybatisplus;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xb.mybatisplus.dto.CreateUserRequest;
import com.xb.mybatisplus.dto.UpdateUserRequest;
import com.xb.mybatisplus.entity.User;
import com.xb.mybatisplus.exception.ResourceNotFoundException;
import com.xb.mybatisplus.mapper.UserMapper;
import com.xb.mybatisplus.service.impl.UserLambdaQueryService;
import com.xb.mybatisplus.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Mapper 集成测试。
 *
 * <p>这里启动真实 Spring 容器、真实 MyBatis 配置和 H2 内存数据库，因此比只测试
 * Java 方法的单元测试更能发现 SQL、XML 映射和插件配置问题，同时绝不修改远程教学库。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
// 强制启用 application-test.yml，使测试与默认 MySQL 配置隔离。
@ActiveProfiles("test")
// 加载完整应用上下文，验证 Starter 自动配置、Mapper 扫描和插件 Bean 能协同工作。
@SpringBootTest
class UserMapperIntegrationTest {
    // JdbcTemplate 只用于准备数据和绕过逻辑删除检查数据库真实值。
    @Autowired private JdbcTemplate jdbcTemplate;
    // 被测对象是真实的 MyBatis Mapper 代理，不是 Mock。
    @Autowired private UserMapper userMapper;
    // 同时注入 Service，验证事务、自动填充、返回值判断和乐观锁业务处理。
    @Autowired private UserService userService;
    // 验证课件重点：Lambda 条件与 BaseMapper 单表查询方法可以组合运行。
    @Autowired private UserLambdaQueryService userLambdaQueryService;

    @BeforeEach
    void prepareTable() {
        // 每个测试开始前重建表，保证测试之间互不依赖，也不受执行顺序影响。
        jdbcTemplate.execute("DROP TABLE IF EXISTS demo_user");
        // H2 开启 MySQL 兼容模式后可覆盖本示例所需的大部分 SQL 语法。
        jdbcTemplate.execute("""
                CREATE TABLE demo_user (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL,
                  age INT NOT NULL, email VARCHAR(100) NOT NULL, status TINYINT DEFAULT 1,
                  created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL,
                  version INT DEFAULT 0, deleted TINYINT DEFAULT 0)
                """);
        // 使用 ? 占位符传值，与 MyBatis 的 #{} 一样属于预编译参数绑定。
        jdbcTemplate.update("INSERT INTO demo_user(name,age,email,status,created_at,updated_at,version,deleted) VALUES (?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)", "张三", 20, "a@example.com", 1);
        jdbcTemplate.update("INSERT INTO demo_user(name,age,email,status,created_at,updated_at,version,deleted) VALUES (?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)", "小明", 16, "b@example.com", 1);
    }

    @Test
    void baseMapperAndLogicDeleteWork() {
        // null Wrapper 表示没有额外业务条件，但逻辑删除条件仍会自动加入。
        List<User> users = userMapper.selectList(null);
        assertThat(users).hasSize(2);

        // deleteById 因 @TableLogic 被改写成 UPDATE deleted=1。
        userMapper.deleteById(users.getFirst().getId());
        // 再使用 BaseMapper 查询时，被逻辑删除的行已经不可见。
        assertThat(userMapper.selectList(null)).hasSize(1);
        // 直接用 JDBC 绕开 MyBatis-Plus，证明记录仍存在，只是 deleted 已变成 1。
        assertThat(jdbcTemplate.queryForObject("SELECT deleted FROM demo_user WHERE id=?", Integer.class, users.getFirst().getId())).isEqualTo(1);
    }

    @Test
    void customXmlAndPaginationWork() {
        // 验证 Mapper 方法、@Param、XML namespace/id 和结果映射全部正确。
        assertThat(userMapper.findActiveAdults(18)).extracting(User::getName).containsExactly("张三");

        // Page.of(1, 1) 表示查询第 1 页、每页 1 条。
        Page<User> page = userMapper.selectPage(Page.of(1, 1), null);
        assertThat(page.getRecords()).hasSize(1);
        // total 来自分页插件自动执行的 COUNT SQL，而不是当前页 records 的大小。
        assertThat(page.getTotal()).isEqualTo(2);
    }

    @Test
    void lambdaSingleTableQueriesWork() {
        // eq + selectOne：邮箱唯一时应准确返回一条记录。
        assertThat(userLambdaQueryService.findOneByEmail("a@example.com"))
                .extracting(User::getName)
                .isEqualTo("张三");

        // like + ge + le + eq + selectList：只保留姓名含“张”、18～25 岁的启用用户。
        assertThat(userLambdaQueryService.search("张", 18, 25, 1))
                .extracting(User::getName)
                .containsExactly("张三");

        // in + selectList：可一次查询多个 id；空 id 集合必须返回空，不能变成全表查询。
        assertThat(userLambdaQueryService.findByIds(List.of(1L, 2L))).hasSize(2);
        assertThat(userLambdaQueryService.findByIds(List.of())).isEmpty();

        // eq + selectCount：统计交给数据库完成。
        assertThat(userLambdaQueryService.countByStatus(1)).isEqualTo(2);

        // 条件分页既返回当前页 records，也返回满足条件的总数 total。
        Page<User> page = userLambdaQueryService.page(1, 1, 1);
        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getTotal()).isEqualTo(2);
    }

    @Test
    void autoFillAndOptimisticLockWorkTogether() {
        // 通过 Service 创建，验证 DTO 转实体、业务默认值、自增主键和自动填充是一条完整链路。
        User created = userService.create(new CreateUserRequest("王老师", 30, "teacher@example.com"));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(1);

        // 数据库默认 version=0；先重新查询，模拟客户端读取最新版本后再提交更新。
        User latest = userService.getRequiredById(created.getId());
        User updated = userService.updateWithOptimisticLock(created.getId(),
                new UpdateUserRequest("王老师（已更新）", 31, "teacher@example.com", latest.getVersion()));
        assertThat(updated.getVersion()).isEqualTo(latest.getVersion() + 1);
        assertThat(updated.getName()).isEqualTo("王老师（已更新）");

        // 再使用已经过期的旧 version 更新，WHERE version=? 无法匹配，Service 应报告冲突。
        assertThatThrownBy(() -> userService.updateWithOptimisticLock(created.getId(),
                new UpdateUserRequest("旧数据覆盖", 32, "teacher@example.com", latest.getVersion())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version 已过期");

        // required 查询把不存在记录转换为明确异常，Controller 会进一步把它映射为 HTTP 404。
        assertThatThrownBy(() -> userService.getRequiredById(99999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("用户不存在");
    }
}
