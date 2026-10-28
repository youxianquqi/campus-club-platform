/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.QuestionnaireQuestion;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionnaireQuestionMapper extends BaseMapper<QuestionnaireQuestion> {

    default List<QuestionnaireQuestion> selectByQuestionnaireId(Integer questionnaireId) {
        return selectList(
                new LambdaQueryWrapper<QuestionnaireQuestion>()
                        .eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId)
                        .orderByAsc(QuestionnaireQuestion::getSortOrder));
    }

    default int countByQuestionnaireId(Integer questionnaireId) {
        return selectCount(
                        new LambdaQueryWrapper<QuestionnaireQuestion>()
                                .eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId))
                .intValue();
    }
}
