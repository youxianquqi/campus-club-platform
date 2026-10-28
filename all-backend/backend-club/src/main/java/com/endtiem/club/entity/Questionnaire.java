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
@TableName("questionnaire")
public class Questionnaire {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer clubId;

    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Integer allowAnonymous;

    private String tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
