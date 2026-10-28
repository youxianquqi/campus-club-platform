/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class QuestionnaireRecommendation {
    private Integer id;
    private Integer clubId;
    private String clubName;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> tags;
    private Integer status;
    private Integer allowAnonymous;
    private Double similarity;
}
