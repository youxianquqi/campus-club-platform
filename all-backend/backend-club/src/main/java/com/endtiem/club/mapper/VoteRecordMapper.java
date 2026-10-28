/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.club.entity.VoteRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoteRecordMapper extends BaseMapper<VoteRecord> {

    default List<VoteRecord> selectByVoteId(Integer voteId) {
        return selectList(
                new LambdaQueryWrapper<VoteRecord>()
                        .eq(VoteRecord::getVoteId, voteId)
                        .orderByDesc(VoteRecord::getVotedAt));
    }

    default VoteRecord selectByVoteIdAndUserId(Integer voteId, Integer userId) {
        return selectOne(
                new LambdaQueryWrapper<VoteRecord>()
                        .eq(VoteRecord::getVoteId, voteId)
                        .eq(VoteRecord::getUserId, userId));
    }

    default int countByVoteId(Integer voteId) {
        return selectCount(new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getVoteId, voteId))
                .intValue();
    }

    default int countByUserId(Integer userId) {
        return selectCount(new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getUserId, userId))
                .intValue();
    }
}
