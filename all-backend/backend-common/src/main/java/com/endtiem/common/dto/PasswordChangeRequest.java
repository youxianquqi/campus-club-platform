/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}
