/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.admin.entity.UserActivityFootprint;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserActivityFootprintMapper extends BaseMapper<UserActivityFootprint> {

    default List<UserActivityFootprint> selectByUserId(Integer userId) {
        return selectList(
                new LambdaQueryWrapper<UserActivityFootprint>()
                        .eq(UserActivityFootprint::getUserId, userId)
                        .orderByDesc(UserActivityFootprint::getCreatedAt));
    }

    default List<UserActivityFootprint> selectByUserIdAndActionType(
            Integer userId, Integer actionType) {
        return selectList(
                new LambdaQueryWrapper<UserActivityFootprint>()
                        .eq(UserActivityFootprint::getUserId, userId)
                        .eq(actionType != null, UserActivityFootprint::getActionType, actionType)
                        .orderByDesc(UserActivityFootprint::getCreatedAt));
    }

    default UserActivityFootprint selectByUserIdAndActivityIdAndActionType(
            Integer userId, Integer activityId, Integer actionType) {
        return selectOne(
                new LambdaQueryWrapper<UserActivityFootprint>()
                        .eq(UserActivityFootprint::getUserId, userId)
                        .eq(UserActivityFootprint::getActivityId, activityId)
                        .eq(UserActivityFootprint::getActionType, actionType));
    }

    default long countByUserIdAndActionType(Integer userId, Integer actionType) {
        return selectCount(
                new LambdaQueryWrapper<UserActivityFootprint>()
                        .eq(UserActivityFootprint::getUserId, userId)
                        .eq(actionType != null, UserActivityFootprint::getActionType, actionType));
    }

    default List<UserActivityFootprint> selectByActivityId(Integer activityId) {
        return selectList(
                new LambdaQueryWrapper<UserActivityFootprint>()
                        .eq(UserActivityFootprint::getActivityId, activityId)
                        .orderByDesc(UserActivityFootprint::getCreatedAt));
    }
}
