/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("invitation")
public class Invitation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer clubId;

    private Integer userId;

    private Integer aiRecommendationId;

    private String message;

    private String code;

    private String status;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;
}
