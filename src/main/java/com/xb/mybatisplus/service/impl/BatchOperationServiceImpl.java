package com.xb.mybatisplus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.xb.mybatisplus.dto.CreateUserRequest;
import com.xb.mybatisplus.entity.User;
import com.xb.mybatisplus.mapper.UserMapper;
import com.xb.mybatisplus.service.BatchOperationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作服务实现。
 *
 * <p>演示 MyBatis-Plus IService 提供的批量操作方法，以及如何使用 LambdaUpdateWrapper
 * 实现条件批量更新。</p>
 *
 * @author xb
 * @since 2026-09-17
 */
@Service
public class BatchOperationServiceImpl extends ServiceImpl<UserMapper, User>
        implements BatchOperationService {

    @Override
    @Transactional
    public List<User> batchCreate(List<CreateUserRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> users = new ArrayList<>();
        for (CreateUserRequest request : requests) {
            User user = new User();
            user.setName(request.name());
            user.setAge(request.age());
            user.setEmail(request.email());
            user.setStatus(1);
            users.add(user);
        }

        // saveBatch 是 IService 提供的批量插入方法，默认每批 1000 条。
        // 底层使用 MyBatis 的 BATCH 模式，比循环调用 insert 更高效。
        if (!saveBatch(users)) {
            throw new IllegalStateException("批量创建用户失败");
        }
        return users;
    }

    @Override
    @Transactional
    public int batchUpdateStatus(List<Long> ids, int status) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // LambdaUpdateWrapper 支持链式调用，构造复杂的 UPDATE 语句。
        // in 条件限定 ID 范围，set 指定要更新的字段。
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(User::getId, ids)
               .set(User::getStatus, status);

        // update 返回受影响的行数。
        return baseMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public int batchRemove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // removeBatchByIds 是 IService 提供的批量删除方法。
        // 因 User 实体使用了 @TableLogic，实际执行的是批量 UPDATE deleted=1。
        return baseMapper.deleteBatchIds(ids);
    }
}
