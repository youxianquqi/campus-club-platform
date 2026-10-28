/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("activity_group_member")
public class ActivityGroupMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    private Long activityId;

    private Integer status;

    private LocalDateTime joinedAt;

    private LocalDateTime createdAt;
}
