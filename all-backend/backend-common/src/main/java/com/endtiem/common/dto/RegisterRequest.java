/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.dto;

import lombok.Data;

/** 与前端注册页对齐：username、password、email、phone。 role、status 若传入也会被忽略，自助注册固定为学生账号。 */
@Data
public class RegisterRequest {

    private String username;
    private String password;
    private String email;
    private String phone;

    /** 前端可能传，后端忽略，防止自助注册提权 */
    private Integer role;

    private Integer status;

    /** 邀请码（可选） */
    private String inviteCode;
}
