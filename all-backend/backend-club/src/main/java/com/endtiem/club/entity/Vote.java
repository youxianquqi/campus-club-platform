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
@TableName("vote")
public class Vote {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer clubId;

    private String title;

    private String description;

    private Integer voteType;

    private Integer maxOptions;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Integer allowAnonymous;

    private Integer showResults;

    private String tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
