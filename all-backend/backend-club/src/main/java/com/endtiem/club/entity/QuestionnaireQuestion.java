/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("questionnaire_question")
public class QuestionnaireQuestion {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer questionnaireId;

    private String questionText;

    private Integer questionType;

    private String options;

    private Integer required;

    private Integer sortOrder;
}
