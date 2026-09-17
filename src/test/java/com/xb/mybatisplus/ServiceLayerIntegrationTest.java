package com.xb.mybatisplus;

import com.xb.mybatisplus.dto.CreateUserRequest;
import com.xb.mybatisplus.entity.User;
import com.xb.mybatisplus.service.BatchOperationService;
import com.xb.mybatisplus.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service 层集成测试。
 *
 * <p>验证批量操作、事务边界和业务默认值的正确性。</p>
 *
 * @author xb
 * @since 2026-09-17
 */
@ActiveProfiles("test")
@SpringBootTest
class ServiceLayerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private BatchOperationService batchOperationService;

    @BeforeEach
    void prepareTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS demo_user");
        jdbcTemplate.execute("""
                CREATE TABLE demo_user (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL,
                  age INT NOT NULL, email VARCHAR(100) NOT NULL, status TINYINT DEFAULT 1,
                  created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL,
                  version INT DEFAULT 0, deleted TINYINT DEFAULT 0)
                """);
    }

    @Test
    @DisplayName("批量创建用户 - 自动填充和默认值")
    void batchCreateUsers() {
        List<CreateUserRequest> requests = List.of(
                new CreateUserRequest("用户A", 20, "a@test.com"),
                new CreateUserRequest("用户B", 22, "b@test.com"),
                new CreateUserRequest("用户C", 24, "c@test.com")
        );

        List<User> created = batchOperationService.batchCreate(requests);

        assertThat(created).hasSize(3);
        assertThat(created).allSatisfy(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getStatus()).isEqualTo(1);
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isNotNull();
        });

        // 验证数据库实际记录数
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM demo_user WHERE deleted=0", Integer.class);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("批量更新状态 - LambdaUpdateWrapper")
    void batchUpdateStatus() {
        // 准备数据
        jdbcTemplate.update(
                "INSERT INTO demo_user(name,age,email,status,created_at,updated_at,version,deleted) " +
                "VALUES (?,20,?,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)", "用户1", "u1@test.com");
        jdbcTemplate.update(
                "INSERT INTO demo_user(name,age,email,status,created_at,updated_at,version,deleted) " +
                "VALUES (?,22,?,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)", "用户2", "u2@test.com");
        jdbcTemplate.update(
                "INSERT INTO demo_user(name,age,email,status,created_at,updated_at,version,deleted) " +
                "VALUES (?,24,?,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)", "用户3", "u3@test.com");

        List<Long> ids = List.of(1L, 2L);
        int updated = batchOperationService.batchUpdateStatus(ids, 0);

        assertThat(updated).isEqualTo(2);

        // 验证状态已更新
        Integer activeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM demo_user WHERE status=1 AND deleted=0", Integer.class);
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("批量逻辑删除 - @TableLogic 生效")
    void batchLogicalDelete() {
        // 准备数据
        jdbcTemplate.update(
                "INSERT INTO demo_user(name,age,email,status,created_at,updated_at,version,deleted) " +
                "VALUES (?,20,?,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)", "用户1", "u1@test.com");
        jdbcTemplate.update(
                "INSERT INTO demo_user(name,age,email,status,created_at,updated_at,version,deleted) " +
                "VALUES (?,22,?,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)", "用户2", "u2@test.com");

        List<Long> ids = List.of(1L, 2L);
        int deleted = batchOperationService.batchRemove(ids);

        assertThat(deleted).isEqualTo(2);

        // 逻辑删除后，普通查询看不到
        List<User> users = userService.list();
        assertThat(users).isEmpty();

        // 但数据库记录仍存在，只是 deleted=1
        Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM demo_user", Integer.class);
        assertThat(totalCount).isEqualTo(2);
    }

    @Test
    @DisplayName("空列表批量操作 - 安全返回")
    void emptyBatchOperations() {
        assertThat(batchOperationService.batchCreate(List.of())).isEmpty();
        assertThat(batchOperationService.batchUpdateStatus(List.of(), 1)).isEqualTo(0);
        assertThat(batchOperationService.batchRemove(List.of())).isEqualTo(0);
    }
}
