/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.entity.RecruitmentApplication;
import com.endtiem.recruitment.entity.RecruitmentPost;
import com.endtiem.recruitment.mapper.RecruitmentApplicationMapper;
import com.endtiem.recruitment.mapper.RecruitmentPostMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/recruitment-posts")
public class RecruitmentApplicationController {

    private final RecruitmentApplicationMapper applicationMapper;
    private final RecruitmentPostMapper postMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public RecruitmentApplicationController(
            RecruitmentApplicationMapper applicationMapper,
            RecruitmentPostMapper postMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil) {
        this.applicationMapper = applicationMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/{postId}/apply")
    public Map<String, Object> apply(
            @PathVariable Integer postId, @RequestBody(required = false) Map<String, Object> body) {
        Integer userId = requireUserIdFromToken();

        RecruitmentPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("招募不存在");
        }
        if (post.getStatus() != 1) {
            throw new RuntimeException("该招募已截止，无法申请");
        }
        if (post.getUserId().equals(userId)) {
            throw new RuntimeException("不能申请自己的招募");
        }

        RecruitmentApplication existing = applicationMapper.selectByPostIdAndUserId(postId, userId);
        if (existing != null) {
            throw new RuntimeException("您已经申请过了");
        }

        String message = body != null ? (String) body.get("message") : null;

        RecruitmentApplication application = new RecruitmentApplication();
        application.setPostId(postId);
        application.setUserId(userId);
        application.setMessage(message);
        application.setStatus(0);
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        applicationMapper.insert(application);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "申请已提交");
        result.put("applicationId", application.getId());
        return result;
    }

    @GetMapping("/{postId}/applications")
    public List<Map<String, Object>> getApplications(@PathVariable Integer postId) {
        requireOwner(postId);

        List<RecruitmentApplication> applications = applicationMapper.selectByPostId(postId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (RecruitmentApplication app : applications) {
            User applicant = userMapper.selectById(app.getUserId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", app.getId());
            row.put("userId", app.getUserId());
            row.put("nickname", applicant != null ? getNickname(applicant) : "用户");
            row.put("avatar", applicant != null ? applicant.getAvatar() : null);
            row.put("message", app.getMessage());
            row.put("status", app.getStatus());
            row.put("statusText", getStatusText(app.getStatus()));
            row.put("createdAt", app.getCreatedAt());
            result.add(row);
        }
        return result;
    }

    @PutMapping("/{postId}/applications/{appId}")
    public Map<String, Object> reviewApplication(
            @PathVariable Integer postId,
            @PathVariable Integer appId,
            @RequestBody Map<String, Object> body) {
        requireOwner(postId);

        RecruitmentApplication application = applicationMapper.selectByIdAndPostId(appId, postId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }
        if (application.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }

        Integer action = toInteger(body.get("action"));
        if (action == null || (action != 1 && action != 2)) {
            throw new RuntimeException("操作无效（1通过 2拒绝）");
        }

        application.setStatus(action);
        application.setUpdatedAt(LocalDateTime.now());
        applicationMapper.updateById(application);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", action == 1 ? "已通过申请" : "已拒绝申请");
        result.put("status", application.getStatus());
        return result;
    }

    @GetMapping("/{postId}/application-status")
    public Map<String, Object> getApplicationStatus(@PathVariable Integer postId) {
        Integer userId = getUserIdOrNull();
        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("hasApplied", false);
            result.put("status", null);
            return result;
        }

        RecruitmentApplication application =
                applicationMapper.selectByPostIdAndUserId(postId, userId);
        if (application == null) {
            result.put("hasApplied", false);
            result.put("status", null);
        } else {
            result.put("hasApplied", true);
            result.put("applicationId", application.getId());
            result.put("status", application.getStatus());
            result.put("statusText", getStatusText(application.getStatus()));
        }

        int pendingCount = applicationMapper.countPendingByPostId(postId);
        result.put("pendingCount", pendingCount);

        return result;
    }

    private void requireOwner(Integer postId) {
        Integer userId = requireUserIdFromToken();
        RecruitmentPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("招募不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("只有发布者可以查看申请列表");
        }
    }

    private Integer requireUserIdFromToken() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        String token = auth.substring(7).trim();
        Integer userId = jwtUtil.getAccountUserIdFromToken(token);
        if (userId == null) {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null || username.isBlank()) {
                throw new RuntimeException("请先登录");
            }
            User user = userMapper.selectByUsername(username.trim());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            userId = user.getId();
        }
        return userId;
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
            return jwtUtil.getAccountUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private String getNickname(User user) {
        if (user == null) {
            return "用户";
        }
        String nick = user.getNickname();
        if (nick != null && !nick.isBlank()) {
            return nick;
        }
        nick = user.getRealName();
        if (nick != null && !nick.isBlank()) {
            return nick;
        }
        return user.getUsername();
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            default:
                return "未知";
        }
    }

    private Integer toInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
