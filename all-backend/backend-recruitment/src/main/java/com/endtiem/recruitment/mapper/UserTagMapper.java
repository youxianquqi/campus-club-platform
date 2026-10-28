/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.recruitment.entity.UserTag;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTagMapper extends BaseMapper<UserTag> {

    default List<UserTag> selectByUserId(Integer userId) {
        return selectList(new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, userId));
    }
}
