/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.controller;

import com.endtiem.admin.entity.UserActivityFootprint;
import com.endtiem.admin.mapper.UserActivityFootprintMapper;
import com.endtiem.club.entity.ClubActivity;
import com.endtiem.club.mapper.ClubActivityMapper;
import com.endtiem.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/footprints")
public class FootprintController {

    private final UserActivityFootprintMapper footprintMapper;
    private final ClubActivityMapper activityMapper;
    private final JwtUtil jwtUtil;

    public FootprintController(
            UserActivityFootprintMapper footprintMapper,
            ClubActivityMapper activityMapper,
            JwtUtil jwtUtil) {
        this.footprintMapper = footprintMapper;
        this.activityMapper = activityMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Map<String, Object>> getFootprints(
            @RequestParam(required = false) Integer actionType) {
        Integer userId = requireUserIdFromToken();
        List<UserActivityFootprint> footprints =
                footprintMapper.selectByUserIdAndActionType(userId, actionType);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserActivityFootprint fp : footprints) {
            ClubActivity activity = activityMapper.selectById(fp.getActivityId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", fp.getId());
            row.put("activityId", fp.getActivityId());
            row.put("actionType", fp.getActionType());
            row.put("actionName", getActionName(fp.getActionType()));
            row.put("createdAt", fp.getCreatedAt());
            if (activity != null) {
                row.put("activityTitle", activity.getTitle());
                row.put("activityTime", activity.getActivityTime());
                row.put("activityLocation", activity.getLocation());
                row.put("clubId", activity.getClubId());
            }
            result.add(row);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> recordFootprint(@RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();
        Integer activityId = toInteger(request.get("activityId"));
        Integer actionType = toInteger(request.get("actionType"));
        if (activityId == null) {
            throw new RuntimeException("缺少活动ID");
        }
        if (actionType == null) {
            actionType = 1;
        }

        ClubActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        UserActivityFootprint existing =
                footprintMapper.selectByUserIdAndActivityIdAndActionType(
                        userId, activityId, actionType);

        if (existing == null) {
            UserActivityFootprint footprint = new UserActivityFootprint();
            footprint.setUserId(userId);
            footprint.setActivityId(activityId);
            footprint.setActionType(actionType);
            footprint.setCreatedAt(now);
            footprintMapper.insert(footprint);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("activityId", activityId);
        result.put("actionType", actionType);
        return result;
    }

    private static String getActionName(Integer actionType) {
        if (actionType == null) return "未知";
        return switch (actionType) {
            case 1 -> "浏览";
            case 2 -> "报名";
            case 3 -> "签到";
            case 4 -> "参与";
            case 5 -> "收藏";
            default -> "其他";
        };
    }

    private Integer requireUserIdFromToken() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        String token = auth.substring(7);
        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    private static Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(o.toString());
    }
}
