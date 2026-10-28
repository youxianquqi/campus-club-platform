/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.common.entity.InviteCode;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InviteCodeMapper extends BaseMapper<InviteCode> {

    default InviteCode selectByCode(String code) {
        return selectOne(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getCode, code)
                        .eq(InviteCode::getStatus, 1));
    }

    default List<InviteCode> selectByCreatorId(Integer creatorId) {
        return selectList(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getCreatorId, creatorId)
                        .orderByDesc(InviteCode::getCreatedAt));
    }

    default List<InviteCode> selectValidByCreatorId(Integer creatorId) {
        return selectList(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getCreatorId, creatorId)
                        .eq(InviteCode::getStatus, 1)
                        .orderByDesc(InviteCode::getCreatedAt));
    }

    default InviteCode selectValidByCode(String code) {
        return selectOne(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getCode, code)
                        .eq(InviteCode::getStatus, 1)
                        .gt(InviteCode::getUsedCount, 0)
                        .or()
                        .eq(InviteCode::getCode, code)
                        .eq(InviteCode::getStatus, 1)
                        .isNull(InviteCode::getExpiresAt)
                        .or()
                        .eq(InviteCode::getCode, code)
                        .eq(InviteCode::getStatus, 1)
                        .gt(InviteCode::getExpiresAt, LocalDateTime.now()));
    }

    default List<InviteCode> selectByClubId(Integer clubId) {
        return selectList(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getClubId, clubId)
                        .eq(InviteCode::getStatus, 1)
                        .orderByDesc(InviteCode::getCreatedAt));
    }

    default void incrementUsedCount(String code) {
        InviteCode inviteCode = selectByCode(code);
        if (inviteCode != null) {
            inviteCode.setUsedCount(inviteCode.getUsedCount() == null ? 1 : inviteCode.getUsedCount() + 1);
            updateById(inviteCode);
        }
    }
}
