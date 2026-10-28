/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/** 社团标签关联 club_tag（见 docs/数据库设计.md 4.6） */
@Data
@TableName("club_tag")
public class ClubTag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer clubId;

    private Integer tagId;

    /** 0 显示，1 隐藏（仍参与匹配） */
    private Boolean isHidden;

    private BigDecimal weight;

    private String description;
}
