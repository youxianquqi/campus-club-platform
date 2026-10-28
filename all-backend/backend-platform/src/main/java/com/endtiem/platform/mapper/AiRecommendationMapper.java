/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.AiRecommendation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiRecommendationMapper extends BaseMapper<AiRecommendation> {

    default List<AiRecommendation> selectByClubId(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<AiRecommendation>().eq(AiRecommendation::getClubId, clubId));
    }

    default AiRecommendation selectByUserIdAndClubId(Integer userId, Integer clubId) {
        return selectOne(
                new LambdaQueryWrapper<AiRecommendation>()
                        .eq(AiRecommendation::getUserId, userId)
                        .eq(AiRecommendation::getClubId, clubId));
    }
}
