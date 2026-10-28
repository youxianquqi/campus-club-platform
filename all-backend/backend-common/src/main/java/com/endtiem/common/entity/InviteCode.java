/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("invite_code")
public class InviteCode {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private Integer creatorId;

    private Integer clubId;

    private Integer maxUses;

    private Integer usedCount;

    private LocalDateTime expiresAt;

    private Integer status;

    private LocalDateTime createdAt;
}
