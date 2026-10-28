/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.Vote;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoteMapper extends BaseMapper<Vote> {

    default List<Vote> selectByClubId(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<Vote>()
                        .eq(Vote::getClubId, clubId)
                        .orderByDesc(Vote::getCreatedAt));
    }

    default List<Vote> selectByClubIdAndStatus(Integer clubId, Integer status) {
        return selectList(
                new LambdaQueryWrapper<Vote>()
                        .eq(Vote::getClubId, clubId)
                        .eq(status != null, Vote::getStatus, status)
                        .orderByDesc(Vote::getCreatedAt));
    }

    default List<Vote> selectActive() {
        return selectList(
                new LambdaQueryWrapper<Vote>()
                        .eq(Vote::getStatus, 1)
                        .orderByDesc(Vote::getCreatedAt));
    }

    default List<Vote> selectByStatus(Integer status) {
        return selectList(
                new LambdaQueryWrapper<Vote>()
                        .eq(status != null, Vote::getStatus, status)
                        .orderByDesc(Vote::getCreatedAt));
    }
}
