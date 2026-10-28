/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.ClubTag;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubTagMapper extends BaseMapper<ClubTag> {

    default List<ClubTag> selectByClubId(Integer clubId) {
        return selectList(new LambdaQueryWrapper<ClubTag>().eq(ClubTag::getClubId, clubId));
    }

    default List<ClubTag> selectByClubIdAndIsHidden(Integer clubId, Boolean isHidden) {
        return selectList(
                new LambdaQueryWrapper<ClubTag>()
                        .eq(ClubTag::getClubId, clubId)
                        .eq(ClubTag::getIsHidden, isHidden));
    }

    default Optional<ClubTag> selectByClubIdAndTagId(Integer clubId, Integer tagId) {
        return Optional.ofNullable(
                selectOne(
                        new LambdaQueryWrapper<ClubTag>()
                                .eq(ClubTag::getClubId, clubId)
                                .eq(ClubTag::getTagId, tagId)));
    }

    default Optional<ClubTag> selectByClubIdAndTagIdAndIsHidden(
            Integer clubId, Integer tagId, Boolean isHidden) {
        return Optional.ofNullable(
                selectOne(
                        new LambdaQueryWrapper<ClubTag>()
                                .eq(ClubTag::getClubId, clubId)
                                .eq(ClubTag::getTagId, tagId)
                                .eq(ClubTag::getIsHidden, isHidden)));
    }

    default int deleteByClubIdAndTagId(Integer clubId, Integer tagId) {
        return delete(
                new LambdaQueryWrapper<ClubTag>()
                        .eq(ClubTag::getClubId, clubId)
                        .eq(ClubTag::getTagId, tagId));
    }
}
