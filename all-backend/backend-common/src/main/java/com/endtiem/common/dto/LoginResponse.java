/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.dto;

import lombok.Data;

@Data
public class LoginResponse {
    /** 与 userId 一致，供前端 user.id 使用（如标签检查） */
    private Integer id;

    private String token;
    private Integer userId;
    private String username;
    private String nickname;
    private Integer role;
    private String avatar;

    /** JWT 业务主体 ID，学生场景与 user.id 一致（原 student 表已合并） */
    private Integer relatedId;

    /** 登录账号主键 user.id（招募帖等绑定 user 表） */
    private Integer accountUserId;
}
