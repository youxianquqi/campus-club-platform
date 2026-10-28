/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("tutorial_progress")
public class TutorialProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer tutorialId;

    private Integer status;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
