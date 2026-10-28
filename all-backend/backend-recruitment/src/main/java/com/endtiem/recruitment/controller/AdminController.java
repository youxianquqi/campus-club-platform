/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.ClubActivity;
import com.endtiem.club.mapper.ClubActivityMapper;
import com.endtiem.common.api.Result;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.entity.ClubApplication;
import com.endtiem.recruitment.mapper.ClubApplicationMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserMapper userMapper;
    private final ClubApplicationMapper clubApplicationMapper;
    private final ClubActivityMapper clubActivityMapper;
    private final JwtUtil jwtUtil;

    public AdminController(
            UserMapper userMapper,
            ClubApplicationMapper clubApplicationMapper,
            ClubActivityMapper clubActivityMapper,
            JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.clubApplicationMapper = clubApplicationMapper;
        this.clubActivityMapper = clubActivityMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
        if (jwtUtil.getRoleFromToken(token) != 1) {
            throw new RuntimeException("您没有权限访问");
        }
        long pendingClubs =
                clubApplicationMapper.selectCount(
                        new LambdaQueryWrapper<ClubApplication>()
                                .eq(ClubApplication::getStatus, 0));
        long pendingActivities =
                clubActivityMapper.selectCount(
                        new LambdaQueryWrapper<ClubActivity>().eq(ClubActivity::getStatus, 0));
        Map<String, Object> data = new HashMap<>();
        data.put("pendingClubs", pendingClubs);
        data.put("pendingActivities", pendingActivities);
        data.put("userCount", userMapper.selectCount(null));
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", data);
        return body;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role != 1) {
            throw new RuntimeException("您没有权限访问");
        }

        List<User> users = userMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("role", user.getRole());
            userMap.put("relatedId", user.getId());
            userMap.put("studentId", user.getStudentId());
            userMap.put("name", user.getRealName());

            result.add(userMap);
        }

        return result;
    }

    @PutMapping("/users/{userId}/role")
    public Result<String> updateUserRole(
            @PathVariable Integer userId, @RequestBody Map<String, Integer> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role != 1) {
            throw new RuntimeException("您没有权限执行此操作");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Integer newRole = request.get("role");
        if (newRole == null) {
            throw new RuntimeException("请提供新的角色值");
        }

        user.setRole(newRole);
        userMapper.updateById(user);

        return Result.ok("用户角色已更新");
    }
}
