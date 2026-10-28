/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class RecruitmentRecommendation {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer clubId;
    private String clubName;
    private String title;
    private String content;
    private Integer type;
    private String typeName;

    private String contact;

    private List<String> tags;
    private LocalDateTime deadline;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private Double similarity;
}
