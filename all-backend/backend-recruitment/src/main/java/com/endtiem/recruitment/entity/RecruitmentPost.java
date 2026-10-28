/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 招募广场帖 recruitment_post */
@Data
@TableName("recruitment_post")
public class RecruitmentPost {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 对应表列 user_id，值为 {@code user.id}（登录用户表） */
    private Integer userId;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private Integer type;

    private Integer status;

    private String contact;

    private Integer clubId;

    private String tags;

    private LocalDateTime deadline;

    private Integer viewCount;

    private LocalDateTime updatedAt;
}
