/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.VoteOption;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoteOptionMapper extends BaseMapper<VoteOption> {

    default List<VoteOption> selectByVoteId(Integer voteId) {
        return selectList(
                new LambdaQueryWrapper<VoteOption>()
                        .eq(VoteOption::getVoteId, voteId)
                        .orderByAsc(VoteOption::getSortOrder));
    }

    default VoteOption selectById(Integer id) {
        return selectById(id);
    }
}
