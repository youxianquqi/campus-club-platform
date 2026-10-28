/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.club.entity.ClubActivity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubActivityMapper extends BaseMapper<ClubActivity> {

    default List<ClubActivity> selectByClubIdOrderByCreatedAtDesc(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<ClubActivity>()
                        .eq(ClubActivity::getClubId, clubId)
                        .orderByDesc(ClubActivity::getCreatedAt));
    }

    default List<ClubActivity> selectByStatusOrderByCreatedAtDesc(Integer status) {
        return selectList(
                new LambdaQueryWrapper<ClubActivity>()
                        .eq(ClubActivity::getStatus, status)
                        .orderByDesc(ClubActivity::getCreatedAt));
    }

    default Page<ClubActivity> pageByStatusOrderByCreatedAtDesc(
            Integer status, Page<ClubActivity> page) {
        return selectPage(
                page,
                new LambdaQueryWrapper<ClubActivity>()
                        .eq(ClubActivity::getStatus, status)
                        .orderByDesc(ClubActivity::getCreatedAt));
    }

    default long countTodayByClubId(Integer clubId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return selectCount(
                new LambdaQueryWrapper<ClubActivity>()
                        .eq(ClubActivity::getClubId, clubId)
                        .ge(ClubActivity::getCreatedAt, startOfDay)
                        .lt(ClubActivity::getCreatedAt, endOfDay));
    }
}
