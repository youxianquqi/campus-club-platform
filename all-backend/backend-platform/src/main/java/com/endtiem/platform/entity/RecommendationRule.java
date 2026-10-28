/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 首页推荐积分规则 recommendation_rule（见 docs/数据库设计.md 4.18） */
@Data
@TableName("recommendation_rule")
public class RecommendationRule {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private BigDecimal maxTagScore;

    private BigDecimal qqBoostScore;

    private BigDecimal hiddenMatchUnit;

    private BigDecimal maxHiddenScore;

    private LocalDateTime updatedAt;
}
