/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.dto;

import lombok.Data;

@Data
public class PeerEvaluationRequest {
    private Integer evaluatedId;
    private String activityType;
    private String customActivity;
    private Integer rating;
    private String comment;
}
