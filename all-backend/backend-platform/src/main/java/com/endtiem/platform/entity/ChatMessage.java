/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 聊天消息 chat_message（见 docs/数据库设计.md 4.13） receiver_id：0 或 null 为历史公共聊天室数据；社团间联系使用 {@link
 * com.endtiem.platform.util.ClubPairChannel} 编码（&gt;0）。
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer senderId;

    private String content;

    private LocalDateTime createdAt;

    /** 0 = 公共聊天室；非 0 可扩展为私聊接收方学生 id */
    private Long receiverId;

    private LocalDateTime sentAt;
}
