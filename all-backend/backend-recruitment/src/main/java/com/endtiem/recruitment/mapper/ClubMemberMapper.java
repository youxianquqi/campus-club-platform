/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.ClubMember;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubMemberMapper extends BaseMapper<ClubMember> {

    default List<ClubMember> selectByClubId(Long clubId) {
        return selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClubMember>()
                        .eq(ClubMember::getClubId, clubId)
                        .eq(ClubMember::getStatus, 1));
    }

    default boolean existsByClubIdAndUserId(Long clubId, Long userId) {
        return selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                                        ClubMember>()
                                .eq(ClubMember::getClubId, clubId)
                                .eq(ClubMember::getUserId, userId)
                                .eq(ClubMember::getStatus, 1))
                > 0;
    }

    /** 在社成员数（status=1），用于首页/列表展示 */
    default long countActiveByClubId(long clubId) {
        return selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClubMember>()
                        .eq(ClubMember::getClubId, clubId)
                        .eq(ClubMember::getStatus, 1));
    }
}
