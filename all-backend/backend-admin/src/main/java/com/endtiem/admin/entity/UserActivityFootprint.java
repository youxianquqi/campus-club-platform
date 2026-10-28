/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user_activity_footprint")
public class UserActivityFootprint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Integer activityId;

    private Integer actionType;

    private LocalDateTime createdAt;
}
