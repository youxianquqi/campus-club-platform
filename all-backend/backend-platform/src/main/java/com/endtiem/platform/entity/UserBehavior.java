/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 用户行为 user_behavior（见 docs/数据库设计.md 4.11） */
@Data
@TableName("user_behavior")
public class UserBehavior {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String pageName;

    private String pageParams;

    private LocalDateTime enterTime;

    private LocalDateTime exitTime;

    private Integer durationSeconds;

    private LocalDateTime createdAt;
}
