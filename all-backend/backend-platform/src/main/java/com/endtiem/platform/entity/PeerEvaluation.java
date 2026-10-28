/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 同伴互评 peer_evaluation（见 docs/数据库设计.md 4.17） */
@Data
@TableName("peer_evaluation")
public class PeerEvaluation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer evaluatorId;

    private Integer evaluatedId;

    private String activityType;

    private String customActivity;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}
