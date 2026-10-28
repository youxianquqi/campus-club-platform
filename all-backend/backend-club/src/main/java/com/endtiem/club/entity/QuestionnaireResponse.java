/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("questionnaire_response")
public class QuestionnaireResponse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer questionnaireId;

    private Integer userId;

    private String anonymousName;

    private String answers;

    private LocalDateTime submittedAt;
}
