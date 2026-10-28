/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.RecommendationRule;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecommendationRuleMapper extends BaseMapper<RecommendationRule> {

    default Optional<RecommendationRule> selectFirstByOrderByIdAsc() {
        List<RecommendationRule> list =
                selectList(
                        new LambdaQueryWrapper<RecommendationRule>()
                                .orderByAsc(RecommendationRule::getId)
                                .last("LIMIT 1"));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
