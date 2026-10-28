/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.ActivityGroup;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityGroupMapper extends BaseMapper<ActivityGroup> {

    default List<ActivityGroup> selectByClubId(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<ActivityGroup>()
                        .eq(ActivityGroup::getClubId, clubId)
                        .orderByDesc(ActivityGroup::getCreatedAt));
    }

    default List<ActivityGroup> selectWithOpenRegistration(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<ActivityGroup>()
                        .eq(ActivityGroup::getClubId, clubId)
                        .ge(ActivityGroup::getMaxSize, 1)
                        .orderByDesc(ActivityGroup::getCreatedAt));
    }
}
