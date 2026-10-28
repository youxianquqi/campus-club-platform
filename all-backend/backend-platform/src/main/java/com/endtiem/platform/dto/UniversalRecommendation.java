/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.dto;

import java.util.List;
import lombok.Data;

@Data
public class UniversalRecommendation {
    private String entityType;
    private Integer id;
    private String title;
    private String description;
    private String subTitle;
    private List<String> tags;
    private Double similarity;
    private Long timestamp;
}
