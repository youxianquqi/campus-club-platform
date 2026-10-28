/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.TutorialProgress;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TutorialProgressMapper extends BaseMapper<TutorialProgress> {

    default List<TutorialProgress> selectByUserId(Long userId) {
        return selectList(
                new LambdaQueryWrapper<TutorialProgress>().eq(TutorialProgress::getUserId, userId));
    }

    default TutorialProgress selectByUserIdAndTutorialId(Long userId, Integer tutorialId) {
        return selectOne(
                new LambdaQueryWrapper<TutorialProgress>()
                        .eq(TutorialProgress::getUserId, userId)
                        .eq(TutorialProgress::getTutorialId, tutorialId));
    }

    default List<TutorialProgress> selectByUserIdAndTutorialIds(
            Long userId, List<Integer> tutorialIds) {
        return selectList(
                new LambdaQueryWrapper<TutorialProgress>()
                        .eq(TutorialProgress::getUserId, userId)
                        .in(TutorialProgress::getTutorialId, tutorialIds));
    }
}
