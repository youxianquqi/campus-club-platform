/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.Announcement;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    default List<Announcement> selectAllOrderByCreatedAtDesc() {
        return selectList(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreatedAt));
    }

    default Announcement selectTopByOrderByCreatedAtDesc() {
        List<Announcement> list =
                selectList(
                        new LambdaQueryWrapper<Announcement>()
                                .orderByDesc(Announcement::getCreatedAt)
                                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }
}
