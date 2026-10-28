/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.ClubAiSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubAiSummaryMapper extends BaseMapper<ClubAiSummary> {

    default ClubAiSummary selectLatestByClubId(Integer clubId) {
        return selectOne(
                new LambdaQueryWrapper<ClubAiSummary>()
                        .eq(ClubAiSummary::getClubId, clubId)
                        .orderByDesc(ClubAiSummary::getGeneratedAt)
                        .last("LIMIT 1"));
    }

    default ClubAiSummary selectByClubIdAndType(Integer clubId, String summaryType) {
        return selectOne(
                new LambdaQueryWrapper<ClubAiSummary>()
                        .eq(ClubAiSummary::getClubId, clubId)
                        .eq(ClubAiSummary::getSummaryType, summaryType)
                        .orderByDesc(ClubAiSummary::getGeneratedAt)
                        .last("LIMIT 1"));
    }
}
