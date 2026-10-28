/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 社团表 club（见 docs/数据库设计.md 4.4） */
@Data
@TableName("club")
public class Club {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String description;

    private String qqGroup;

    private String logoUrl;

    private Integer adminId;

    private LocalDateTime createdAt;
}
