/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RecruitmentPostRequest {

    private String title;

    private String content;

    private Integer type;

    private String tags;

    private LocalDateTime deadline;
}
