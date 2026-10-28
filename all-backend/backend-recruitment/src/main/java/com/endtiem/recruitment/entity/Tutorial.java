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
@TableName("tutorial")
public class Tutorial {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String content;

    private Integer category;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
