package com.xb.mybatisplus.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.xb.mybatisplus.dto.CreateUserRequest;
import com.xb.mybatisplus.dto.UpdateUserRequest;
import com.xb.mybatisplus.entity.User;
import com.xb.mybatisplus.exception.ResourceNotFoundException;
import com.xb.mybatisplus.mapper.UserMapper;
import com.xb.mybatisplus.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务实现。
 *
 * <p>ServiceImpl 的两个泛型分别是 Mapper 类型和实体类型。继承后即可复用 save、list、
 * page、updateById 等方法；真正的业务默认值、并发判断和事务边界仍由本类负责。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    @Transactional
    public User create(CreateUserRequest request) {
        // DTO 描述外部输入，Entity 描述数据库记录；这里是两种模型的转换边界。
        User user = new User();
        user.setName(request.name());
        user.setAge(request.age());
        user.setEmail(request.email());

        // status 不允许客户端随意提交，由业务层赋予新用户“启用”的默认状态。
        user.setStatus(1);

        // save 最终调用 UserMapper.insert。执行前 MetaObjectHandler 会填充时间字段；
        // 执行成功后，MySQL 自增 id 会被回填到同一个 user 对象。
        // 虽然正常情况下 insert 会成功，但教学代码仍检查返回值，避免静默失败。
        if (!save(user)) {
            throw new IllegalStateException("创建用户失败，请稍后重试");
        }
        return user;
    }

    @Override
    public User getRequiredById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在：id=" + id);
        }
        return user;
    }

    @Override
    @Transactional
    public void removeRequiredById(Long id) {
        // removeById 返回 false 表示没有记录受到影响，常见原因是记录不存在或已经逻辑删除。
        if (!removeById(id)) {
            throw new ResourceNotFoundException("用户不存在或已经删除：id=" + id);
        }
    }

    @Override
    @Transactional
    public User updateWithOptimisticLock(Long id, UpdateUserRequest request) {
        // 这里只构造需要更新的字段。MyBatis-Plus 默认不会把 null 字段写入 SET 子句。
        User user = new User();
        user.setId(id);
        user.setName(request.name());
        user.setAge(request.age());
        user.setEmail(request.email());

        // version 是客户端最近一次查询到的旧版本，也是本次更新的并发条件。
        user.setVersion(request.version());

        // 乐观锁插件把 SQL 改写为类似：
        // UPDATE ... SET ..., version=version+1 WHERE id=? AND version=? AND deleted=0。
        // 如果期间别人已更新过该行，旧 version 匹配不到记录，updateById 返回 false。
        if (!updateById(user)) {
            throw new IllegalStateException("更新失败：数据不存在，或 version 已过期，请重新查询后再修改");
        }

        // 再查询一次，返回数据库中的最新状态，包括新 version 和自动更新的 updatedAt。
        return getRequiredById(id);
    }
}
