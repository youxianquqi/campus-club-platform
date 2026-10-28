/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.ActivityWork;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityWorkMapper extends BaseMapper<ActivityWork> {

    default ActivityWork selectByActivityIdAndUserId(Integer activityId, Integer userId) {
        return selectOne(
                new LambdaQueryWrapper<ActivityWork>()
                        .eq(ActivityWork::getActivityId, activityId)
                        .eq(ActivityWork::getUserId, userId)
                        .eq(ActivityWork::getStatus, 1));
    }

    default long countByActivityId(Integer activityId) {
        return selectCount(
                new LambdaQueryWrapper<ActivityWork>()
                        .eq(ActivityWork::getActivityId, activityId)
                        .eq(ActivityWork::getStatus, 1));
    }

    default long countByUserId(Integer userId) {
        return selectCount(
                new LambdaQueryWrapper<ActivityWork>()
                        .eq(ActivityWork::getUserId, userId)
                        .eq(ActivityWork::getStatus, 1));
    }
}
