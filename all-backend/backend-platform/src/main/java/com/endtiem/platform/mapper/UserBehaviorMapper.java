/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.UserBehavior;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    default List<UserBehavior> selectByUserId(Integer userId) {
        return selectList(
                new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getUserId, userId));
    }

    default List<UserBehavior> selectByUserIdAndPageName(Integer userId, String pageName) {
        return selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getPageName, pageName));
    }

    default List<UserBehavior> selectByPageName(String pageName) {
        return selectList(
                new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getPageName, pageName));
    }

    default UserBehavior selectFirstByUserIdOrderByEnterTimeDesc(Integer userId) {
        List<UserBehavior> list =
                selectList(
                        new LambdaQueryWrapper<UserBehavior>()
                                .eq(UserBehavior::getUserId, userId)
                                .orderByDesc(UserBehavior::getEnterTime)
                                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }
}
