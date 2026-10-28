/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.dto;

import lombok.Data;

@Data
public class StudentInfoRequest {
    /** 与前端 realName 对齐，写入 user.real_name */
    private String realName;

    private String nickname;
    private String phone;
    private String college;
    private String major;
    private Integer grade;
    private String specialty;
    private String hobby;
    private String expectation;
    private String avatar;
}
