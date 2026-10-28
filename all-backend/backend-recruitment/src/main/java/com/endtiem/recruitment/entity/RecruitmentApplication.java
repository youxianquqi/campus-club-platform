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
@TableName("recruitment_application")
public class RecruitmentApplication {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer postId;

    private Integer userId;

    private String message;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
