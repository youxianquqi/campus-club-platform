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
@TableName("activity_group")
public class ActivityGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer activityId;

    private Integer clubId;

    private Integer creatorId;

    private String name;

    private String description;

    private Integer minSize;

    private Integer maxSize;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
