/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.entity.Tutorial;
import com.endtiem.recruitment.entity.TutorialProgress;
import com.endtiem.recruitment.mapper.TutorialMapper;
import com.endtiem.recruitment.mapper.TutorialProgressMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/tutorials")
public class TutorialController {

    private final TutorialMapper tutorialMapper;
    private final TutorialProgressMapper tutorialProgressMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public TutorialController(
            TutorialMapper tutorialMapper,
            TutorialProgressMapper tutorialProgressMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil) {
        this.tutorialMapper = tutorialMapper;
        this.tutorialProgressMapper = tutorialProgressMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) Integer category) {
        List<Tutorial> tutorials;
        if (category != null) {
            tutorials = tutorialMapper.selectByCategory(category);
        } else {
            tutorials = tutorialMapper.selectPublishedList();
        }
        Long userId = getCurrentUserIdOrNull();
        List<TutorialProgress> progressList = new ArrayList<>();
        if (userId != null && !tutorials.isEmpty()) {
            List<Integer> tutorialIds =
                    tutorials.stream().map(Tutorial::getId).collect(Collectors.toList());
            progressList = tutorialProgressMapper.selectByUserIdAndTutorialIds(userId, tutorialIds);
        }
        final List<TutorialProgress> finalProgressList = progressList;
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tutorial tutorial : tutorials) {
            TutorialProgress progress =
                    finalProgressList.stream()
                            .filter(p -> p.getTutorialId().equals(tutorial.getId()))
                            .findFirst()
                            .orElse(null);
            result.add(toRow(tutorial, progress));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Tutorial tutorial = tutorialMapper.selectById(id);
        if (tutorial == null) {
            throw new RuntimeException("教程不存在");
        }
        Long userId = getCurrentUserIdOrNull();
        TutorialProgress progress = null;
        if (userId != null) {
            progress = tutorialProgressMapper.selectByUserIdAndTutorialId(userId.longValue(), id);
        }
        return toDetailRow(tutorial, progress);
    }

    @GetMapping("/progress")
    public List<Map<String, Object>> getProgress() {
        Long userId = requireUserIdFromToken();
        List<TutorialProgress> progressList = tutorialProgressMapper.selectByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (TutorialProgress progress : progressList) {
            Tutorial tutorial = tutorialMapper.selectById(progress.getTutorialId());
            if (tutorial != null) {
                Map<String, Object> row = new HashMap<>();
                row.put("tutorialId", progress.getTutorialId());
                row.put("title", tutorial.getTitle());
                row.put("category", tutorial.getCategory());
                row.put("status", progress.getStatus());
                row.put("readAt", progress.getReadAt());
                result.add(row);
            }
        }
        return result;
    }

    @PostMapping("/{id}/progress")
    public Map<String, Object> markAsRead(@PathVariable Integer id) {
        Long userId = requireUserIdFromToken();
        Tutorial tutorial = tutorialMapper.selectById(id);
        if (tutorial == null) {
            throw new RuntimeException("教程不存在");
        }

        TutorialProgress progress = tutorialProgressMapper.selectByUserIdAndTutorialId(userId, id);
        LocalDateTime now = LocalDateTime.now();
        if (progress == null) {
            progress = new TutorialProgress();
            progress.setUserId(userId);
            progress.setTutorialId(id);
            progress.setStatus(1);
            progress.setReadAt(now);
            progress.setCreatedAt(now);
            progress.setUpdatedAt(now);
            tutorialProgressMapper.insert(progress);
        } else {
            if (progress.getStatus() == 0) {
                progress.setStatus(1);
                progress.setReadAt(now);
            }
            progress.setUpdatedAt(now);
            tutorialProgressMapper.updateById(progress);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tutorialId", id);
        result.put("status", progress.getStatus());
        return result;
    }

    @PutMapping("/{id}/favorite")
    public Map<String, Object> toggleFavorite(@PathVariable Integer id) {
        Long userId = requireUserIdFromToken();
        Tutorial tutorial = tutorialMapper.selectById(id);
        if (tutorial == null) {
            throw new RuntimeException("教程不存在");
        }

        TutorialProgress progress = tutorialProgressMapper.selectByUserIdAndTutorialId(userId, id);
        LocalDateTime now = LocalDateTime.now();
        if (progress == null) {
            progress = new TutorialProgress();
            progress.setUserId(userId);
            progress.setTutorialId(id);
            progress.setStatus(2);
            progress.setReadAt(now);
            progress.setCreatedAt(now);
            progress.setUpdatedAt(now);
            tutorialProgressMapper.insert(progress);
        } else {
            if (progress.getStatus() == 2) {
                progress.setStatus(1);
            } else {
                progress.setStatus(2);
            }
            progress.setUpdatedAt(now);
            tutorialProgressMapper.updateById(progress);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tutorialId", id);
        result.put("status", progress.getStatus());
        result.put("isFavorite", progress.getStatus() == 2);
        return result;
    }

    private Long getCurrentUserIdOrNull() {
        try {
            HttpServletRequest httpRequest =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                            .getRequest();
            String auth = httpRequest.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                return null;
            }
            String token = auth.substring(7).trim();
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null || username.isBlank()) {
                return null;
            }
            User user = userMapper.selectByUsername(username.trim());
            if (user == null) {
                return null;
            }
            return user.getId().longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Long requireUserIdFromToken() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        String token = auth.substring(7).trim();
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || username.isBlank()) {
            throw new RuntimeException("请先登录");
        }
        User user = userMapper.selectByUsername(username.trim());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId().longValue();
    }

    private Map<String, Object> toRow(Tutorial tutorial, TutorialProgress progress) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", tutorial.getId());
        row.put("title", tutorial.getTitle());
        row.put("category", tutorial.getCategory());
        row.put("sortOrder", tutorial.getSortOrder());
        row.put("status", tutorial.getStatus());
        if (progress != null) {
            row.put("progressStatus", progress.getStatus());
            row.put("readAt", progress.getReadAt());
        } else {
            row.put("progressStatus", 0);
        }
        return row;
    }

    private Map<String, Object> toDetailRow(Tutorial tutorial, TutorialProgress progress) {
        Map<String, Object> row = toRow(tutorial, progress);
        row.put("content", tutorial.getContent());
        row.put("createdAt", tutorial.getCreatedAt());
        row.put("updatedAt", tutorial.getUpdatedAt());
        return row;
    }
}
