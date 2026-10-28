/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.Questionnaire;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {

    default List<Questionnaire> selectByClubId(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<Questionnaire>()
                        .eq(Questionnaire::getClubId, clubId)
                        .orderByDesc(Questionnaire::getCreatedAt));
    }

    default List<Questionnaire> selectByClubIdAndStatus(Integer clubId, Integer status) {
        return selectList(
                new LambdaQueryWrapper<Questionnaire>()
                        .eq(Questionnaire::getClubId, clubId)
                        .eq(status != null, Questionnaire::getStatus, status)
                        .orderByDesc(Questionnaire::getCreatedAt));
    }

    default List<Questionnaire> selectActive() {
        return selectList(
                new LambdaQueryWrapper<Questionnaire>()
                        .eq(Questionnaire::getStatus, 1)
                        .orderByDesc(Questionnaire::getCreatedAt));
    }

    default List<Questionnaire> selectByStatus(Integer status) {
        return selectList(
                new LambdaQueryWrapper<Questionnaire>()
                        .eq(status != null, Questionnaire::getStatus, status)
                        .orderByDesc(Questionnaire::getCreatedAt));
    }
}
