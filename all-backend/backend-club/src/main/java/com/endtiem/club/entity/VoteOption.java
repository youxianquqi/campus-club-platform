/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vote_option")
public class VoteOption {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer voteId;

    private String optionText;

    private String imageUrl;

    private Integer voteCount;

    private Integer sortOrder;
}
