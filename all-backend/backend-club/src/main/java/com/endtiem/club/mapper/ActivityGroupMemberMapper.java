/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.ActivityGroupMember;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityGroupMemberMapper extends BaseMapper<ActivityGroupMember> {

    default List<ActivityGroupMember> selectByGroupId(Long groupId) {
        return selectList(
                new LambdaQueryWrapper<ActivityGroupMember>()
                        .eq(ActivityGroupMember::getGroupId, groupId)
                        .eq(ActivityGroupMember::getStatus, 1));
    }

    default List<ActivityGroupMember> selectByUserId(Long userId) {
        return selectList(
                new LambdaQueryWrapper<ActivityGroupMember>()
                        .eq(ActivityGroupMember::getUserId, userId)
                        .eq(ActivityGroupMember::getStatus, 1));
    }

    default long countActiveMembers(Long groupId) {
        return selectCount(
                new LambdaQueryWrapper<ActivityGroupMember>()
                        .eq(ActivityGroupMember::getGroupId, groupId)
                        .eq(ActivityGroupMember::getStatus, 1));
    }

    default ActivityGroupMember selectByGroupIdAndUserId(Long groupId, Long userId) {
        return selectOne(
                new LambdaQueryWrapper<ActivityGroupMember>()
                        .eq(ActivityGroupMember::getGroupId, groupId)
                        .eq(ActivityGroupMember::getUserId, userId));
    }
}
