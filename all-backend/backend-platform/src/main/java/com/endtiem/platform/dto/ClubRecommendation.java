/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.dto;

import java.util.List;
import lombok.Data;

@Data
public class ClubRecommendation {
    private Integer id;
    private String name;
    private String description;
    private List<Integer> tags;
    private Double similarity;
}
