/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.MembershipApplication;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MembershipApplicationMapper extends BaseMapper<MembershipApplication> {

    default List<MembershipApplication> selectByUserId(Integer userId) {
        return selectList(
                new LambdaQueryWrapper<MembershipApplication>()
                        .eq(MembershipApplication::getUserId, userId));
    }

    default List<MembershipApplication> selectByClubId(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<MembershipApplication>()
                        .eq(MembershipApplication::getClubId, clubId));
    }

    default List<MembershipApplication> selectByClubIdAndStatus(Integer clubId, Integer status) {
        return selectList(
                new LambdaQueryWrapper<MembershipApplication>()
                        .eq(MembershipApplication::getClubId, clubId)
                        .eq(MembershipApplication::getStatus, status));
    }

    default Optional<MembershipApplication> selectByUserIdAndClubId(
            Integer userId, Integer clubId) {
        return Optional.ofNullable(
                selectOne(
                        new LambdaQueryWrapper<MembershipApplication>()
                                .eq(MembershipApplication::getUserId, userId)
                                .eq(MembershipApplication::getClubId, clubId)));
    }

    default long countByClubIdAndStatus(Integer clubId, Integer status) {
        return selectCount(
                new LambdaQueryWrapper<MembershipApplication>()
                        .eq(MembershipApplication::getClubId, clubId)
                        .eq(MembershipApplication::getStatus, status));
    }
}
