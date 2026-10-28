/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.Tutorial;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TutorialMapper extends BaseMapper<Tutorial> {

    default List<Tutorial> selectPublishedList() {
        return selectList(
                new LambdaQueryWrapper<Tutorial>()
                        .eq(Tutorial::getStatus, 1)
                        .orderByAsc(Tutorial::getSortOrder));
    }

    default List<Tutorial> selectByCategory(Integer category) {
        return selectList(
                new LambdaQueryWrapper<Tutorial>()
                        .eq(category != null, Tutorial::getCategory, category)
                        .eq(Tutorial::getStatus, 1)
                        .orderByAsc(Tutorial::getSortOrder));
    }
}
