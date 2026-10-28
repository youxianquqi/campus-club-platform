/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.RecruitmentApplication;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecruitmentApplicationMapper extends BaseMapper<RecruitmentApplication> {

    default List<RecruitmentApplication> selectByPostId(Integer postId) {
        return selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                                RecruitmentApplication>()
                        .eq(RecruitmentApplication::getPostId, postId)
                        .orderByDesc(RecruitmentApplication::getCreatedAt));
    }

    default RecruitmentApplication selectByPostIdAndUserId(Integer postId, Integer userId) {
        return selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                                RecruitmentApplication>()
                        .eq(RecruitmentApplication::getPostId, postId)
                        .eq(RecruitmentApplication::getUserId, userId));
    }

    default RecruitmentApplication selectByIdAndPostId(Integer id, Integer postId) {
        return selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                                RecruitmentApplication>()
                        .eq(RecruitmentApplication::getId, id)
                        .eq(RecruitmentApplication::getPostId, postId));
    }

    default int countPendingByPostId(Integer postId) {
        Long count =
                selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                                        RecruitmentApplication>()
                                .eq(RecruitmentApplication::getPostId, postId)
                                .eq(RecruitmentApplication::getStatus, 0));
        return count != null ? count.intValue() : 0;
    }
}
