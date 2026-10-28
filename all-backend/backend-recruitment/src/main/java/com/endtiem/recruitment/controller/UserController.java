/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.api.Result;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.dto.StudentInfoRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController("recruitmentUserController")
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final JwtUtil jwtUtil;

    public UserController(UserMapper userMapper, ClubMapper clubMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.clubMapper = clubMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/info")
    public User getUserInfo() {
        Integer userId = resolveUserIdFromRequest();
        return userMapper.selectById(userId);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("studentId", user.getStudentId());
        result.put("realName", user.getRealName());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("college", user.getCollege());
        result.put("major", user.getMajor());
        result.put("grade", user.getGrade());
        result.put("specialty", user.getSpecialty());
        result.put("hobby", user.getHobby());
        result.put("expectation", user.getExpectation());
        result.put("role", user.getRole());
        result.put("createdAt", user.getCreatedAt());

        return result;
    }

    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody StudentInfoRequest request) {
        Integer userId = resolveUserIdFromRequest();
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new RuntimeException("用户信息不存在");
        }

        if (request.getRealName() != null && !request.getRealName().isBlank()) {
            user.setRealName(request.getRealName().trim());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getCollege() != null) {
            user.setCollege(request.getCollege());
        }
        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }
        if (request.getGrade() != null) {
            user.setGrade(request.getGrade());
        }
        if (request.getSpecialty() != null) {
            user.setSpecialty(request.getSpecialty());
        }
        if (request.getHobby() != null) {
            user.setHobby(request.getHobby());
        }
        if (request.getExpectation() != null) {
            user.setExpectation(request.getExpectation());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);

        return Result.ok("信息更新成功");
    }

    @PutMapping("/avatar")
    public Result<String> updateAvatar(@RequestBody Map<String, String> request) {
        Integer userId = resolveUserIdFromRequest();
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new RuntimeException("用户信息不存在");
        }

        String avatar = request.get("avatar");
        if (avatar == null || avatar.isEmpty()) {
            throw new RuntimeException("头像URL不能为空");
        }

        user.setAvatar(avatar);
        userMapper.updateById(user);

        return Result.ok("头像更新成功");
    }

    @PutMapping("/recommend-boost")
    public Map<String, Object> saveRecommendBoost(@RequestBody Map<String, Object> body) {
        Integer userId = resolveUserIdFromRequest();
        Object raw = body != null ? body.get("clubId") : null;
        if (raw == null) {
            throw new RuntimeException("clubId不能为空");
        }
        int clubId;
        if (raw instanceof Number) {
            clubId = ((Number) raw).intValue();
        } else {
            try {
                clubId = Integer.parseInt(String.valueOf(raw).trim());
            } catch (NumberFormatException e) {
                throw new RuntimeException("clubId格式错误");
            }
        }
        if (clubMapper.selectById(clubId) == null) {
            throw new RuntimeException("社团不存在");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户信息不存在");
        }
        user.setRecommendBoostClubId(clubId);
        userMapper.updateById(user);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("clubId", clubId);
        return res;
    }

    /** 解析当前登录用户 {@code user.id}（与 JWT accountUserId / relatedId 一致） */
    private Integer resolveUserIdFromRequest() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
        Integer accountId = jwtUtil.getAccountUserIdFromToken(token);
        if (accountId != null) {
            return accountId;
        }
        return jwtUtil.getRelatedIdFromToken(token);
    }
}
