/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 建社申请 club_application（见 docs/数据库设计.md 4.10） */
@Data
@TableName("club_application")
public class ClubApplication {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer applicantId;

    private String name;

    private String description;

    private String tags;

    /** 0 待审 / 1 通过 / 2 未通过 */
    private Integer status;

    private String rejectReason;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private Integer reviewerId;

    private Integer createdClubId;
}
