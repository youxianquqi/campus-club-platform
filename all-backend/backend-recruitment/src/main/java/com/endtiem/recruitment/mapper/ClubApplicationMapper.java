/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.ClubApplication;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubApplicationMapper extends BaseMapper<ClubApplication> {

    default List<ClubApplication> selectByStatus(Integer status) {
        return selectList(
                new LambdaQueryWrapper<ClubApplication>().eq(ClubApplication::getStatus, status));
    }

    default List<ClubApplication> selectByApplicantId(Integer applicantId) {
        return selectList(
                new LambdaQueryWrapper<ClubApplication>()
                        .eq(ClubApplication::getApplicantId, applicantId));
    }
}
