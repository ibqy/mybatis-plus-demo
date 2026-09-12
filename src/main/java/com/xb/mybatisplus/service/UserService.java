package com.xb.mybatisplus.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.xb.mybatisplus.dto.CreateUserRequest;
import com.xb.mybatisplus.dto.UpdateUserRequest;
import com.xb.mybatisplus.entity.User;

/**
 * 用户业务接口。
 *
 * <p>IService 在 BaseMapper 之上提供 save、list、page、removeById 等通用能力；
 * 这里再声明本项目自己的业务动作，让 Controller 依赖业务语义而不是数据库细节。</p>
 *
 * @author xb
 * @since 2026-08-12
 */
public interface UserService extends IService<User> {

    /** 把经过校验的创建请求转换为实体并保存。 */
    User create(CreateUserRequest request);

    /** 按主键查询；不存在时抛出业务异常，而不是向接口返回含义模糊的 null。 */
    User getRequiredById(Long id);

    /** 按主键逻辑删除；不存在或已经删除时明确报告失败。 */
    void removeRequiredById(Long id);

    /** 按客户端携带的 version 执行乐观锁更新，冲突时明确失败。 */
    User updateWithOptimisticLock(Long id, UpdateUserRequest request);
}
