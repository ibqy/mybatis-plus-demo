package com.xb.mybatisplus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xb.mybatisplus.dto.CreateUserRequest;
import com.xb.mybatisplus.dto.UpdateUserRequest;
import com.xb.mybatisplus.entity.User;
import com.xb.mybatisplus.mapper.UserMapper;
import com.xb.mybatisplus.service.impl.UserLambdaQueryService;
import com.xb.mybatisplus.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户教学接口。
 *
 * <p>{@code @RestController} 表示方法返回值会由 Jackson 转换为 JSON，而不是被当作页面名称。
 * {@code @RequestMapping("/api")} 为本类所有接口添加统一前缀。</p>
 *
 * <p>接口按 demo1 到 demo5 逐步增加能力，建议配合 docs 教学课件练习。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
@RestController
@RequestMapping("/api")
// @Validated 让 @RequestParam、@PathVariable 等方法参数上的约束注解生效。
@Validated
public class UserLessonController {
    // Controller 通过业务 Service 完成常规操作，避免在 Web 层堆积业务规则。
    private final UserService userService;
    // Lambda 单表查询集中放在教学服务中，方便学员对照 BaseMapper 的四种查询方法。
    private final UserLambdaQueryService userLambdaQueryService;
    // 额外注入 Mapper 仅用于展示“自定义 XML SQL”的直接调用方式。
    private final UserMapper userMapper;

    // 构造器注入使依赖关系清晰，也方便测试时传入替代对象。
    // 当一个类只有一个构造器时，Spring 不要求再写 @Autowired。
    public UserLessonController(UserService userService,
                                UserLambdaQueryService userLambdaQueryService,
                                UserMapper userMapper) {
        this.userService = userService;
        this.userLambdaQueryService = userLambdaQueryService;
        this.userMapper = userMapper;
    }

    /** Demo 1：BaseMapper/IService 的无条件列表和主键查询。 */
    @GetMapping("/demo1/users")
    public List<User> list() {
        // list() 来自 IService，底层相当于 BaseMapper.selectList(null)。
        // 因为 User 有 @TableLogic，最终 SQL 会自动包含 deleted=0。
        return userService.list();
    }

    /** @PathVariable 把 URL 路径中的 {id} 转换为 Long 参数。 */
    @GetMapping("/demo1/users/{id}")
    public User getById(@PathVariable Long id) {
        // Service 会把“不存在”转换成 ResourceNotFoundException，最终响应 HTTP 404。
        return userService.getRequiredById(id);
    }

    /** Demo 2：新增。@Valid 会先校验 DTO，非法数据不会进入数据库。 */
    @PostMapping("/demo2/users")
    // REST 语义中，成功创建资源通常返回 201，而不是普通的 200。
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody CreateUserRequest request) {
        // @RequestBody 把 JSON 反序列化为 DTO；@Valid 随后执行 DTO 上的校验注解。
        return userService.create(request);
    }

    /** Demo 3：Lambda 多条件查询，参数不传时对应条件不会加入 SQL。 */
    @GetMapping("/demo3/users/search")
    public List<User> search(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer minAge,
                             @RequestParam(required = false) Integer maxAge,
                             @RequestParam(required = false) Integer status) {
        return userLambdaQueryService.search(keyword, minAge, maxAge, status);
    }

    /** Demo 3：eq + selectOne，适合用唯一字段快速查询一条记录。 */
    @GetMapping("/demo3/users/by-email")
    public User findByEmail(@RequestParam String email) {
        return userLambdaQueryService.findOneByEmail(email);
    }

    /** Demo 3：in + selectList，一次查询多个指定主键。请求示例：ids=1,2,3。 */
    @GetMapping("/demo3/users/by-ids")
    public List<User> findByIds(@RequestParam List<Long> ids) {
        return userLambdaQueryService.findByIds(ids);
    }

    /** Demo 3：eq + selectCount，让数据库直接完成数量统计。 */
    @GetMapping("/demo3/users/count")
    public long count(@RequestParam(required = false) Integer status) {
        return userLambdaQueryService.countByStatus(status);
    }

    /** Demo 4：分页。current 从 1 开始，size 最大值在插件配置中限制为 100。 */
    @GetMapping("/demo4/users/page")
    public Page<User> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "current 必须从 1 开始") long current,
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "size 不能小于 1")
            @Max(value = 100, message = "size 不能大于 100") long size,
            @RequestParam(required = false) Integer status) {
        // Page.of 的 current 从 1 开始。分页插件通常先查询 COUNT(*)，再用 LIMIT 查询当前页。
        // Page 返回值中既有 records，也有 total、pages、current、size 等分页元数据。
        // status 不传时不拼接状态条件；传入时演示 Lambda 条件与分页组合。
        return userLambdaQueryService.page(current, size, status);
    }

    /** Demo 5：自动填充 + 乐观锁更新。 */
    @PutMapping("/demo5/users/{id}")
    public User update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        // 并发判断属于业务规则，因此交给 Service，而不是直接在 Controller 调 Mapper。
        return userService.updateWithOptimisticLock(id, request);
    }

    /** Demo 5：逻辑删除。数据库记录仍在，但 deleted 变成 1。 */
    @DeleteMapping("/demo5/users/{id}")
    // 204 表示请求成功，但响应体为空；因此该 Java 方法返回 void。
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // 方法名虽然是 remove，但 @TableLogic 会把物理 DELETE 改为逻辑 UPDATE。
        userService.removeRequiredById(id);
    }

    /** 拓展：调用 resources/mapper/UserMapper.xml 中的自定义 SQL。 */
    @GetMapping("/demo5/users/active-adults")
    public List<User> activeAdults(@RequestParam(defaultValue = "18") int minAge) {
        // Mapper 方法与 XML 中 namespace + id 相匹配，#{minAge} 使用预编译参数绑定。
        return userMapper.findActiveAdults(minAge);
    }
}
