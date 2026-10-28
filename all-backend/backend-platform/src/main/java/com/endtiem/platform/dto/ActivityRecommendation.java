/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ActivityRecommendation {
    private Integer id;
    private Integer clubId;
    private String clubName;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime activityTime;
    private String location;
    private List<String> tags;
    private Integer participants;
    private Integer maxParticipants;
    private Double similarity;
}
