/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StudentLoginRequest {

    /** 登录账号：用户名、学号或手机号（兼容历史字段 studentNo） */
    @JsonProperty("username")
    @JsonAlias({"studentNo"})
    private String username;

    private String password;
}
