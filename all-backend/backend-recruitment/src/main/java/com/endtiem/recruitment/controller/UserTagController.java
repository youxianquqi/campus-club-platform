/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.endtiem.common.api.Result;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.dto.StudentTagRequest;
import com.endtiem.recruitment.entity.UserTag;
import com.endtiem.recruitment.mapper.UserTagMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/user/tags")
public class UserTagController {

    private final UserTagMapper userTagMapper;
    private final JwtUtil jwtUtil;

    public UserTagController(UserTagMapper userTagMapper, JwtUtil jwtUtil) {
        this.userTagMapper = userTagMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Result<List<Integer>> getUserTags() {
        Integer userId = resolveUserIdFromRequest();
        List<Integer> ids = List.of();
        if (userId != null) {
            ids =
                    userTagMapper.selectByUserId(userId).stream()
                            .map(UserTag::getTagId)
                            .collect(Collectors.toList());
        }
        return Result.ok(ids);
    }

    @GetMapping("/check")
    public Result<Map<String, Boolean>> checkUserTags() {
        Integer userId = resolveUserIdFromRequest();
        if (userId == null) {
            return Result.ok(Map.of("hasTags", false));
        }
        List<UserTag> existing = userTagMapper.selectByUserId(userId);
        return Result.ok(Map.of("hasTags", !existing.isEmpty()));
    }

    @PostMapping
    public Result<String> saveUserTags(@RequestBody StudentTagRequest request) {
        Integer userId = resolveUserIdFromRequest();
        if (userId == null) {
            return Result.fail(401, "未登录或无效用户");
        }
        if (request.getTagIds() == null || request.getTagIds().isEmpty()) {
            return Result.fail(400, "请至少选择一个标签");
        }

        List<UserTag> existing = userTagMapper.selectByUserId(userId);
        if (!existing.isEmpty()) {
            return Result.fail(400, "您已经选择过标签，请使用更新功能修改");
        }

        for (Integer tagId : request.getTagIds()) {
            UserTag userTag = new UserTag();
            userTag.setUserId(userId);
            userTag.setTagId(tagId);
            userTagMapper.insert(userTag);
        }

        return Result.ok("标签保存成功");
    }

    @PutMapping
    public Result<String> updateUserTags(@RequestBody StudentTagRequest request) {
        Integer userId = resolveUserIdFromRequest();
        if (userId == null) {
            return Result.fail(401, "未登录或无效用户");
        }
        if (request.getTagIds() == null || request.getTagIds().isEmpty()) {
            return Result.fail(400, "请至少选择一个标签");
        }

        List<UserTag> existing = userTagMapper.selectByUserId(userId);
        if (!existing.isEmpty()) {
            for (UserTag tag : existing) {
                userTagMapper.deleteById(tag.getId());
            }
        }

        for (Integer tagId : request.getTagIds()) {
            UserTag userTag = new UserTag();
            userTag.setUserId(userId);
            userTag.setTagId(tagId);
            userTagMapper.insert(userTag);
        }

        return Result.ok("标签更新成功");
    }

    @DeleteMapping
    public Result<String> deleteUserTags() {
        Integer userId = resolveUserIdFromRequest();
        if (userId == null) {
            return Result.fail(401, "未登录或无效用户");
        }

        List<UserTag> existing = userTagMapper.selectByUserId(userId);
        for (UserTag tag : existing) {
            userTagMapper.deleteById(tag.getId());
        }

        return Result.ok("标签已清除");
    }

    private Integer resolveUserIdFromRequest() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        String token = auth.substring(7);
        Integer accountId = jwtUtil.getAccountUserIdFromToken(token);
        if (accountId != null) {
            return accountId;
        }
        return jwtUtil.getRelatedIdFromToken(token);
    }
}
