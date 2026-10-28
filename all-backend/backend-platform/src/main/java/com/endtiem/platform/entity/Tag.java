/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 兴趣标签字典 tag（见 docs/数据库设计.md 4.1） */
@Data
@TableName("tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private LocalDateTime createdAt;
}
