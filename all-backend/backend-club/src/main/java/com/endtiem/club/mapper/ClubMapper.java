/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.Club;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubMapper extends BaseMapper<Club> {

    default List<Club> selectByNameContaining(String name) {
        return selectList(new LambdaQueryWrapper<Club>().like(Club::getName, name));
    }

    default Club selectByAdminId(Integer adminId) {
        return selectOne(new LambdaQueryWrapper<Club>().eq(Club::getAdminId, adminId));
    }
}
