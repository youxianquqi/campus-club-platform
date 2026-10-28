/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController("adminUserController")
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserController(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        String token = auth.substring(7).trim();

        Integer role = jwtUtil.getRoleFromToken(token);

        Integer accountId = jwtUtil.getAccountUserIdFromToken(token);
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);
        Integer uid = accountId != null ? accountId : relatedId;

        User user = null;
        if (uid != null) {
            user = userMapper.selectById(uid);
        }
        if (user == null) {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null && !username.isBlank()) {
                user = userMapper.selectByUsername(username.trim());
            }
        }
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        String displayName =
                user.getRealName() != null && !user.getRealName().isBlank()
                        ? user.getRealName()
                        : user.getUsername();
        String sid =
                user.getStudentId() != null && !user.getStudentId().isBlank()
                        ? user.getStudentId()
                        : user.getUsername();

        result.put("id", user.getId());
        result.put("studentId", sid);
        result.put("username", user.getUsername());
        result.put("name", displayName);
        result.put("realName", displayName);
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        result.put("college", user.getCollege());
        result.put("major", user.getMajor());
        result.put("grade", user.getGrade());
        result.put("specialty", user.getSpecialty());
        result.put("hobby", user.getHobby());
        result.put("expectation", user.getExpectation());
        result.put("role", role);
        return result;
    }

    @GetMapping("/{id:\\d+}")
    public Map<String, Object> getUserById(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("studentId", user.getStudentId());
        result.put("name", user.getRealName());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        result.put("college", user.getCollege());
        result.put("major", user.getMajor());
        result.put("grade", user.getGrade());
        result.put("specialty", user.getSpecialty());
        result.put("hobby", user.getHobby());
        result.put("expectation", user.getExpectation());

        return result;
    }
}
