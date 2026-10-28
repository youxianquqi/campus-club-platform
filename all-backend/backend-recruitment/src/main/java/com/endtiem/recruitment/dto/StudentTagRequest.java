/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.dto;

import java.util.List;
import lombok.Data;

@Data
public class StudentTagRequest {
    private List<Integer> tagIds;
}
