/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.Invitation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvitationMapper extends BaseMapper<Invitation> {

    default List<Invitation> selectByUserId(Integer userId) {
        return selectList(new LambdaQueryWrapper<Invitation>().eq(Invitation::getUserId, userId));
    }

    default List<Invitation> selectByClubId(Integer clubId) {
        return selectList(new LambdaQueryWrapper<Invitation>().eq(Invitation::getClubId, clubId));
    }

    default List<Invitation> selectByUserIdAndStatus(Integer userId, String status) {
        return selectList(
                new LambdaQueryWrapper<Invitation>()
                        .eq(Invitation::getUserId, userId)
                        .eq(Invitation::getStatus, status));
    }

    default Invitation selectByClubIdAndUserId(Integer clubId, Integer userId) {
        return selectOne(
                new LambdaQueryWrapper<Invitation>()
                        .eq(Invitation::getClubId, clubId)
                        .eq(Invitation::getUserId, userId));
    }

    default Invitation selectByCode(String code) {
        return selectOne(new LambdaQueryWrapper<Invitation>().eq(Invitation::getCode, code));
    }
}
