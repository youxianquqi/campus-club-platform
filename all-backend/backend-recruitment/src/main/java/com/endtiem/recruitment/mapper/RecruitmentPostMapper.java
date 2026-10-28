/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.RecruitmentPost;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecruitmentPostMapper extends BaseMapper<RecruitmentPost> {

    default List<RecruitmentPost> selectLatest(int limit) {
        int n = Math.min(Math.max(limit, 1), 500);
        return selectList(
                new LambdaQueryWrapper<RecruitmentPost>()
                        .orderByDesc(RecruitmentPost::getCreatedAt)
                        .last("LIMIT " + n));
    }

    default List<RecruitmentPost> selectByType(Integer type, int limit) {
        int n = Math.min(Math.max(limit, 1), 500);
        return selectList(
                new LambdaQueryWrapper<RecruitmentPost>()
                        .eq(type != null, RecruitmentPost::getType, type)
                        .orderByDesc(RecruitmentPost::getCreatedAt)
                        .last("LIMIT " + n));
    }

    default List<RecruitmentPost> selectByStatus(Integer status, int limit) {
        int n = Math.min(Math.max(limit, 1), 500);
        return selectList(
                new LambdaQueryWrapper<RecruitmentPost>()
                        .eq(status != null, RecruitmentPost::getStatus, status)
                        .orderByDesc(RecruitmentPost::getCreatedAt)
                        .last("LIMIT " + n));
    }

    default List<RecruitmentPost> selectByTypeAndStatus(Integer type, Integer status, int limit) {
        int n = Math.min(Math.max(limit, 1), 500);
        return selectList(
                new LambdaQueryWrapper<RecruitmentPost>()
                        .eq(type != null, RecruitmentPost::getType, type)
                        .eq(status != null, RecruitmentPost::getStatus, status)
                        .orderByDesc(RecruitmentPost::getCreatedAt)
                        .last("LIMIT " + n));
    }

    default List<RecruitmentPost> selectActiveOrderByCreatedAtDesc(int limit) {
        int n = Math.min(Math.max(limit, 1), 500);
        return selectList(
                new LambdaQueryWrapper<RecruitmentPost>()
                        .eq(RecruitmentPost::getStatus, 1)
                        .orderByDesc(RecruitmentPost::getCreatedAt)
                        .last("LIMIT " + n));
    }

    default List<RecruitmentPost> selectActivePosts() {
        return selectList(
                new LambdaQueryWrapper<RecruitmentPost>()
                        .eq(RecruitmentPost::getStatus, 1)
                        .orderByDesc(RecruitmentPost::getCreatedAt));
    }

    default long countTodayByUserId(Integer userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return selectCount(
                new LambdaQueryWrapper<RecruitmentPost>()
                        .eq(RecruitmentPost::getUserId, userId)
                        .ge(RecruitmentPost::getCreatedAt, startOfDay)
                        .lt(RecruitmentPost::getCreatedAt, endOfDay));
    }
}
