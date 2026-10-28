/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.Tag;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    default Optional<Tag> selectByName(String name) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name)));
    }
}
