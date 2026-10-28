/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.platform.entity.ChatMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    default Page<ChatMessage> pageAllOrderByCreatedAtDesc(Page<ChatMessage> page) {
        return selectPage(
                page, new LambdaQueryWrapper<ChatMessage>().orderByDesc(ChatMessage::getCreatedAt));
    }

    /** 公共聊天室：receiver_id 为 0 或未设置（兼容旧数据） */
    default Page<ChatMessage> pagePublicRoomOrderByCreatedAtDesc(Page<ChatMessage> page) {
        return selectPage(
                page,
                new LambdaQueryWrapper<ChatMessage>()
                        .and(
                                w ->
                                        w.isNull(ChatMessage::getReceiverId)
                                                .or()
                                                .eq(ChatMessage::getReceiverId, 0L))
                        .orderByDesc(ChatMessage::getCreatedAt));
    }

    /** 社团对频道：receiver_id 为 ClubPairChannel.encode(a,b) */
    default Page<ChatMessage> pageByClubChannelOrderByCreatedAtDesc(
            long channelKey, Page<ChatMessage> page) {
        return selectPage(
                page,
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getReceiverId, channelKey)
                        .orderByDesc(ChatMessage::getCreatedAt));
    }

    @Select(
            "SELECT DISTINCT receiver_id FROM chat_message WHERE receiver_id IS NOT NULL AND"
                    + " receiver_id <> 0")
    List<Long> selectDistinctClubChannelKeys();

    default List<ChatMessage> selectAllOrderByCreatedAtAsc() {
        return selectList(
                new LambdaQueryWrapper<ChatMessage>().orderByAsc(ChatMessage::getCreatedAt));
    }

    @Select("SELECT * FROM chat_message ORDER BY created_at DESC LIMIT #{limit}")
    List<ChatMessage> selectRecentMessages(@Param("limit") Integer limit);
}
