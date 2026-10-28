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
@TableName("club_question")
public class ClubQuestion {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer clubId;

    private Integer userId;

    private String content;

    private String answer;

    private LocalDateTime answeredAt;

    private LocalDateTime createdAt;
}
