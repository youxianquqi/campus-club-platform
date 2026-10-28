/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.ActivitySignup;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivitySignupMapper extends BaseMapper<ActivitySignup> {

    default ActivitySignup selectByActivityIdAndUserId(Integer activityId, Integer userId) {
        return selectOne(
                new LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getActivityId, activityId)
                        .eq(ActivitySignup::getUserId, userId));
    }

    default long countByActivityId(Integer activityId) {
        return selectCount(
                new LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getActivityId, activityId)
                        .eq(ActivitySignup::getStatus, 1));
    }

    default long countByUserId(Integer userId) {
        return selectCount(
                new LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getUserId, userId)
                        .eq(ActivitySignup::getStatus, 1));
    }

    default int deleteByActivityIdAndUserId(Integer activityId, Integer userId) {
        return delete(
                new LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getActivityId, activityId)
                        .eq(ActivitySignup::getUserId, userId));
    }
}
