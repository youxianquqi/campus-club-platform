/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 社团活动 club_activity（见 docs/数据库设计.md 4.14） */
@Data
@TableName("club_activity")
public class ClubActivity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer clubId;

    private String title;

    private String content;

    private String imageUrl;

    private LocalDateTime activityTime;

    private String location;

    private Integer currentParticipants;

    private Integer maxParticipants;

    /** 0 待审 / 1 通过 / 2 拒绝 */
    private Integer status;

    private String rejectReason;

    private String tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime reviewedAt;
}
