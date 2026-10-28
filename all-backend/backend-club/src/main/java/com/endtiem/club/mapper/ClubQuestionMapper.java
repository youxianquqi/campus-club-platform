/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.club.entity.ClubQuestion;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubQuestionMapper extends BaseMapper<ClubQuestion> {

    default Page<ClubQuestion> pageByClubIdOrderByCreatedAtDesc(
            Integer clubId, Page<ClubQuestion> page) {
        return selectPage(
                page,
                new LambdaQueryWrapper<ClubQuestion>()
                        .eq(ClubQuestion::getClubId, clubId)
                        .orderByDesc(ClubQuestion::getCreatedAt));
    }

    default List<ClubQuestion> selectByClubIdOrderByCreatedAtDesc(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<ClubQuestion>()
                        .eq(ClubQuestion::getClubId, clubId)
                        .orderByDesc(ClubQuestion::getCreatedAt));
    }

    default List<ClubQuestion> selectByUserIdOrderByCreatedAtDesc(Integer userId) {
        return selectList(
                new LambdaQueryWrapper<ClubQuestion>()
                        .eq(ClubQuestion::getUserId, userId)
                        .orderByDesc(ClubQuestion::getCreatedAt));
    }
}
