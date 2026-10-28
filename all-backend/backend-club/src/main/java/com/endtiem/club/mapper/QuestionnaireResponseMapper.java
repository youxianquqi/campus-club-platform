/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.QuestionnaireResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionnaireResponseMapper extends BaseMapper<QuestionnaireResponse> {

    default List<QuestionnaireResponse> selectByQuestionnaireId(Integer questionnaireId) {
        return selectList(
                new LambdaQueryWrapper<QuestionnaireResponse>()
                        .eq(QuestionnaireResponse::getQuestionnaireId, questionnaireId)
                        .orderByDesc(QuestionnaireResponse::getSubmittedAt));
    }

    default QuestionnaireResponse selectByQuestionnaireIdAndUserId(
            Integer questionnaireId, Integer userId) {
        return selectOne(
                new LambdaQueryWrapper<QuestionnaireResponse>()
                        .eq(QuestionnaireResponse::getQuestionnaireId, questionnaireId)
                        .eq(QuestionnaireResponse::getUserId, userId));
    }

    default int countByQuestionnaireId(Integer questionnaireId) {
        return selectCount(
                        new LambdaQueryWrapper<QuestionnaireResponse>()
                                .eq(QuestionnaireResponse::getQuestionnaireId, questionnaireId))
                .intValue();
    }

    default int countByUserId(Integer userId) {
        return selectCount(
                        new LambdaQueryWrapper<QuestionnaireResponse>()
                                .eq(QuestionnaireResponse::getUserId, userId))
                .intValue();
    }
}
