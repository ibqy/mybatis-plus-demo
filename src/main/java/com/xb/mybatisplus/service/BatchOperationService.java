package com.xb.mybatisplus.service;

import com.xb.mybatisplus.dto.CreateUserRequest;
import com.xb.mybatisplus.entity.User;

import java.util.List;

/**
 * 批量操作服务接口。
 *
 * <p>演示 MyBatis-Plus 的批量操作能力，包括批量插入、批量更新。</p>
 *
 * @author xb
 * @since 2026-09-17
 */
public interface BatchOperationService {

    /**
     * 批量创建用户。
     *
     * @param requests 创建请求列表
     * @return 创建成功的用户列表（含自增 ID）
     */
    List<User> batchCreate(List<CreateUserRequest> requests);

    /**
     * 批量更新用户状态。
     *
     * @param ids 用户 ID 列表
     * @param status 目标状态
     * @return 更新的记录数
     */
    int batchUpdateStatus(List<Long> ids, int status);

    /**
     * 批量逻辑删除。
     *
     * @param ids 用户 ID 列表
     * @return 删除的记录数
     */
    int batchRemove(List<Long> ids);
}
