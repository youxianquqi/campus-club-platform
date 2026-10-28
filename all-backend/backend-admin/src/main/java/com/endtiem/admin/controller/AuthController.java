/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.common.dto.LoginResponse;
import com.endtiem.common.dto.RegisterRequest;
import com.endtiem.common.dto.StudentLoginRequest;
import com.endtiem.common.entity.InviteCode;
import com.endtiem.common.mapper.InviteCodeMapper;
import com.endtiem.common.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final InviteCodeMapper inviteCodeMapper;

    public AuthController(
            UserMapper userMapper, InviteCodeMapper inviteCodeMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.inviteCodeMapper = inviteCodeMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody StudentLoginRequest loginRequest) {
        if (loginRequest.getUsername() == null
                || loginRequest.getUsername().isBlank()
                || loginRequest.getPassword() == null
                || loginRequest.getPassword().isEmpty()) {
            throw new RuntimeException("请输入用户名和密码");
        }

        User user = resolveLoginUser(loginRequest.getUsername());

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!Objects.equals(user.getPassword(), loginRequest.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token =
                jwtUtil.generateToken(
                        user.getUsername(), user.getRole(), user.getId(), user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setRelatedId(user.getId());
        response.setAccountUserId(user.getId());
        response.setUserId(user.getId());
        response.setId(user.getId());

        String nick = user.getNickname();
        if (nick == null || nick.isBlank()) {
            nick = user.getRealName();
        }
        if (nick == null || nick.isBlank()) {
            nick = user.getUsername();
        }
        response.setNickname(nick);
        response.setAvatar(user.getAvatar());

        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", response);
        return body;
    }

    /** 按用户名、学号或手机号解析登录用户（user 表为准） */
    private User resolveLoginUser(String rawAccount) {
        if (rawAccount == null || rawAccount.isBlank()) {
            return null;
        }
        String account = rawAccount.trim();

        User user = userMapper.selectByUsername(account);
        if (user != null) {
            return user;
        }

        user = userMapper.selectByStudentId(account);
        if (user != null) {
            return user;
        }

        return userMapper.selectByPhone(account);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String password = request.getPassword() != null ? request.getPassword() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String phone = request.getPhone() != null ? request.getPhone().trim() : "";

        if (username.isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new RuntimeException("密码长度至少6位");
        }
        if (email.isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (!email.contains("@")) {
            throw new RuntimeException("请输入有效邮箱");
        }
        if (phone.isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }

        if (userMapper.selectByUsername(username) != null) {
            throw new RuntimeException("该用户名已注册");
        }
        if (userMapper.selectByStudentId(username) != null) {
            throw new RuntimeException("该用户名已被占用");
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new RuntimeException("该邮箱已被使用");
        }
        if (userMapper.selectByPhone(phone) != null) {
            throw new RuntimeException("该手机号已注册");
        }

        String inviteCode = request.getInviteCode();
        Integer invitedBy = null;
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            InviteCode code = inviteCodeMapper.selectByCode(inviteCode.trim());
            if (code != null && code.getStatus() == 1) {
                if (code.getExpiresAt() == null
                        || code.getExpiresAt().isAfter(LocalDateTime.now())) {
                    if (code.getUsedCount() < code.getMaxUses()) {
                        invitedBy = code.getCreatorId();
                    }
                }
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(0);
        user.setStudentId(username);
        user.setRealName(username);
        user.setNickname(username);
        user.setPhone(phone);
        user.setEmail(email);
        user.setInviteCodeUsed(invitedBy != null ? 1 : 0);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        if (invitedBy != null) {
            user.setInvitedBy(invitedBy);
            userMapper.updateById(user);

            InviteCode code = inviteCodeMapper.selectByCode(inviteCode.trim());
            if (code != null) {
                code.setUsedCount(code.getUsedCount() + 1);
                inviteCodeMapper.updateById(code);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "注册成功");
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());

        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", data);
        return body;
    }
}
