/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("`user`")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String studentId;

    private String realName;

    private String nickname;

    private String idCard;

    private String password;

    private String avatar;

    private String phone;

    private String email;

    private String college;

    private String major;

    private Integer grade;

    private String specialty;

    private String hobby;

    private String expectation;

    private Integer recommendBoostClubId;

    private Integer role;

    private Integer invitedBy;

    private Integer inviteCodeUsed;

    private LocalDateTime createdAt;
}
