/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.dto.RecruitmentPostRequest;
import com.endtiem.recruitment.entity.RecruitmentPost;
import com.endtiem.recruitment.entity.UserTag;
import com.endtiem.recruitment.mapper.RecruitmentPostMapper;
import com.endtiem.recruitment.mapper.UserTagMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/recruitment-posts")
public class RecruitmentPostController {

    private static final int LIST_LIMIT = 200;
    private static final int CONTENT_MAX = 8000;
    private static final int TITLE_MAX = 200;

    private final RecruitmentPostMapper recruitmentPostMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final UserTagMapper userTagMapper;

    public RecruitmentPostController(
            RecruitmentPostMapper recruitmentPostMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil,
            UserTagMapper userTagMapper) {
        this.recruitmentPostMapper = recruitmentPostMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.userTagMapper = userTagMapper;
    }

    @GetMapping
    public List<Map<String, Object>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "false") boolean sortByMatch) {
        List<RecruitmentPost> posts;
        if (type != null && status != null) {
            posts = recruitmentPostMapper.selectByTypeAndStatus(type, status, LIST_LIMIT);
        } else if (type != null) {
            posts = recruitmentPostMapper.selectByType(type, LIST_LIMIT);
        } else if (status != null) {
            posts = recruitmentPostMapper.selectByStatus(status, LIST_LIMIT);
        } else {
            posts = recruitmentPostMapper.selectLatest(LIST_LIMIT);
        }

        if (sortByMatch) {
            Integer userId = getUserIdOrNull();
            if (userId != null) {
                Set<Integer> userTagIds = getUserTagIds(userId);
                if (!userTagIds.isEmpty()) {
                    return posts.stream()
                            .map(
                                    post -> {
                                        Set<Integer> postTagIds = parseTagsToIds(post.getTags());
                                        double similarity =
                                                calculateJaccardSimilarity(userTagIds, postTagIds);
                                        User author = userMapper.selectById(post.getUserId());
                                        Map<String, Object> row = toRow(post, author);
                                        row.put("similarity", similarity);
                                        return row;
                                    })
                            .filter(row -> (Double) row.get("similarity") > 0)
                            .sorted(
                                    (a, b) ->
                                            Double.compare(
                                                    (Double) b.get("similarity"),
                                                    (Double) a.get("similarity")))
                            .collect(Collectors.toList());
                }
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (RecruitmentPost p : posts) {
            User author = userMapper.selectById(p.getUserId());
            out.add(toRow(p, author));
        }
        return out;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        RecruitmentPost post = recruitmentPostMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("招募不存在");
        }
        if (post.getViewCount() == null) {
            post.setViewCount(0);
        }
        post.setViewCount(post.getViewCount() + 1);
        recruitmentPostMapper.updateById(post);
        User author = userMapper.selectById(post.getUserId());
        return toDetailRow(post, author);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody RecruitmentPostRequest req) {
        Integer accountUserId = requireAccountUserIdFromToken();
        String content = req.getContent() == null ? "" : req.getContent().trim();
        if (content.isEmpty()) {
            throw new RuntimeException("招募内容不能为空");
        }
        if (content.length() > CONTENT_MAX) {
            throw new RuntimeException("招募内容过长（最多 " + CONTENT_MAX + " 字）");
        }
        String title = req.getTitle() == null ? null : req.getTitle().trim();
        if (title != null && title.length() > TITLE_MAX) {
            throw new RuntimeException("标题过长（最多 " + TITLE_MAX + " 字）");
        }
        if (title != null && title.isEmpty()) {
            title = null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todayCount = recruitmentPostMapper.countTodayByUserId(accountUserId, startOfDay, endOfDay);
        if (todayCount > 0) {
            throw new RuntimeException("您今天已经发布过招募，每天只能发布一个招募");
        }

        RecruitmentPost post = new RecruitmentPost();
        post.setUserId(accountUserId);
        post.setTitle(title);
        post.setContent(content);
        post.setType(req.getType());
        post.setStatus(1);
        post.setTags(req.getTags());
        post.setDeadline(req.getDeadline());
        post.setViewCount(0);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        recruitmentPostMapper.insert(post);

        User author = userMapper.selectById(accountUserId);
        return toRow(post, author);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(
            @PathVariable Integer id, @RequestBody Map<String, Object> req) {
        Integer accountUserId = requireAccountUserIdFromToken();
        RecruitmentPost post = recruitmentPostMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("招募不存在");
        }
        if (!post.getUserId().equals(accountUserId)) {
            throw new RuntimeException("无权修改他人的招募");
        }

        if (req.get("title") != null) {
            String title = req.get("title").toString().trim();
            if (title.length() > TITLE_MAX) {
                throw new RuntimeException("标题过长（最多 " + TITLE_MAX + " 字）");
            }
            post.setTitle(title.isEmpty() ? null : title);
        }
        if (req.get("content") != null) {
            String content = req.get("content").toString().trim();
            if (content.length() > CONTENT_MAX) {
                throw new RuntimeException("招募内容过长（最多 " + CONTENT_MAX + " 字）");
            }
            post.setContent(content);
        }
        if (req.get("type") != null) {
            post.setType(toInteger(req.get("type")));
        }
        if (req.get("tags") != null) {
            post.setTags(req.get("tags").toString());
        }
        if (req.get("deadline") != null) {
            post.setDeadline(toLocalDateTime(req.get("deadline")));
        }

        post.setUpdatedAt(LocalDateTime.now());
        recruitmentPostMapper.updateById(post);

        User author = userMapper.selectById(post.getUserId());
        return toRow(post, author);
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Integer id, @RequestBody Map<String, Object> req) {
        Integer accountUserId = requireAccountUserIdFromToken();
        RecruitmentPost post = recruitmentPostMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("招募不存在");
        }
        if (!post.getUserId().equals(accountUserId)) {
            throw new RuntimeException("无权修改他人的招募状态");
        }

        Integer newStatus = toInteger(req.get("status"));
        if (newStatus == null || (newStatus != 1 && newStatus != 2)) {
            throw new RuntimeException("状态值无效（1进行中 2已截止）");
        }
        post.setStatus(newStatus);
        post.setUpdatedAt(LocalDateTime.now());
        recruitmentPostMapper.updateById(post);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", post.getId());
        result.put("status", post.getStatus());
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        Integer accountUserId = requireAccountUserIdFromToken();
        RecruitmentPost post = recruitmentPostMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("招募不存在");
        }
        if (!post.getUserId().equals(accountUserId)) {
            throw new RuntimeException("无权删除他人的招募");
        }
        recruitmentPostMapper.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private String bearerToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        return auth.substring(7).trim();
    }

    private Integer requireAccountUserIdFromToken() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = bearerToken(httpRequest);
        Integer fromClaim = jwtUtil.getAccountUserIdFromToken(token);
        if (fromClaim != null) {
            return fromClaim;
        }
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || username.isBlank()) {
            throw new RuntimeException("请先登录");
        }
        User user = userMapper.selectByUsername(username.trim());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }

    private Map<String, Object> toRow(RecruitmentPost p, User author) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", p.getId());
        row.put("userId", p.getUserId());
        String nickname = "用户";
        String avatar = null;
        if (author != null) {
            String nick = author.getNickname();
            if (nick == null || nick.isBlank()) {
                nick = author.getRealName();
            }
            nickname = nick != null && !nick.isBlank() ? nick : author.getUsername();
            avatar = author.getAvatar();
        }
        row.put("nickname", nickname);
        row.put("avatar", avatar);
        row.put("title", p.getTitle());
        row.put("content", p.getContent());
        row.put("type", p.getType());
        row.put("status", p.getStatus());
        row.put("tags", p.getTags());
        row.put("deadline", p.getDeadline());
        row.put("viewCount", p.getViewCount());
        row.put("createdAt", p.getCreatedAt());
        row.put("updatedAt", p.getUpdatedAt());
        return row;
    }

    private Map<String, Object> toDetailRow(RecruitmentPost p, User author) {
        Map<String, Object> row = toRow(p, author);
        return row;
    }

    private static Integer toInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return Integer.parseInt(o.toString());
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        long ms;
        if (value instanceof Number) {
            ms = ((Number) value).longValue();
        } else {
            ms = Long.parseLong(value.toString());
        }
        return java.time.Instant.ofEpochMilli(ms)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Integer getUserIdOrNull() {
        try {
            HttpServletRequest httpRequest =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                            .getRequest();
            String auth = httpRequest.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                return null;
            }
            String token = auth.substring(7).trim();
            Integer userId = jwtUtil.getRelatedIdFromToken(token);
            return userId;
        } catch (Exception e) {
            return null;
        }
    }

    private Set<Integer> getUserTagIds(int userId) {
        List<UserTag> userTags = userTagMapper.selectByUserId(userId);
        return userTags.stream().map(UserTag::getTagId).collect(Collectors.toSet());
    }

    private Set<Integer> parseTagsToIds(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return new HashSet<>();
        }
        Set<Integer> result = new HashSet<>();
        String[] parts = tags.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    result.add(Integer.parseInt(trimmed));
                } catch (NumberFormatException e) {
                    // 忽略非数字标签
                }
            }
        }
        return result;
    }

    private double calculateJaccardSimilarity(Set<Integer> set1, Set<Integer> set2) {
        if (set1 == null || set2 == null || set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }
        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }
}
