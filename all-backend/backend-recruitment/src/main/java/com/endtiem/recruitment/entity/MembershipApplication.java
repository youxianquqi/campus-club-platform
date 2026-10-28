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
@TableName("application")
public class MembershipApplication {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer clubId;

    private String selfIntro;

    private String phone;

    private Integer status;

    private String rejectReason;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;
}
