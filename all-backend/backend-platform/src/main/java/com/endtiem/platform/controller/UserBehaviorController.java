/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.entity.UserBehavior;
import com.endtiem.platform.mapper.UserBehaviorMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
@RequestMapping("/api/behavior")
public class UserBehaviorController {

    private final UserBehaviorMapper userBehaviorMapper;
    private final JwtUtil jwtUtil;

    public UserBehaviorController(UserBehaviorMapper userBehaviorMapper, JwtUtil jwtUtil) {
        this.userBehaviorMapper = userBehaviorMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/enter")
    public Map<String, Object> recordEnter(@RequestBody Map<String, String> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        String pageName = request.get("pageName");
        String pageParams = request.get("pageParams");

        LocalDateTime now = LocalDateTime.now();
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setPageName(pageName);
        behavior.setPageParams(pageParams);
        behavior.setEnterTime(now);
        behavior.setDurationSeconds(0);
        behavior.setCreatedAt(now);

        userBehaviorMapper.insert(behavior);

        Map<String, Object> result = new HashMap<>();
        result.put("id", behavior.getId());
        result.put("message", "进入页面记录成功");
        return result;
    }

    @PutMapping("/exit/{id}")
    public Map<String, String> recordExit(@PathVariable Integer id) {
        UserBehavior behavior = userBehaviorMapper.selectById(id);
        if (behavior == null) {
            throw new RuntimeException("行为记录不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        behavior.setExitTime(now);

        long durationSeconds = 0;
        if (behavior.getEnterTime() != null) {
            durationSeconds = ChronoUnit.SECONDS.between(behavior.getEnterTime(), now);
        }
        behavior.setDurationSeconds((int) durationSeconds);

        userBehaviorMapper.updateById(behavior);

        Map<String, String> result = new HashMap<>();
        result.put("message", "退出页面记录成功");
        result.put("duration", durationSeconds + "秒");
        return result;
    }

    @GetMapping("/my")
    public List<UserBehavior> getMyBehaviors() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        return userBehaviorMapper.selectByUserId(userId);
    }

    @GetMapping("/my/page/{pageName}")
    public List<UserBehavior> getMyBehaviorsByPage(@PathVariable String pageName) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        return userBehaviorMapper.selectByUserIdAndPageName(userId, pageName);
    }

    @GetMapping("/stats/page/{pageName}")
    public Map<String, Object> getPageStats(@PathVariable String pageName) {
        List<UserBehavior> behaviors = userBehaviorMapper.selectByPageName(pageName);

        long totalDuration = 0;
        int count = behaviors.size();

        for (UserBehavior behavior : behaviors) {
            if (behavior.getDurationSeconds() != null) {
                totalDuration += behavior.getDurationSeconds();
            }
        }

        double avgDuration = count > 0 ? (double) totalDuration / count : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("pageName", pageName);
        result.put("visitCount", count);
        result.put("totalDurationSeconds", totalDuration);
        result.put("avgDurationSeconds", avgDuration);
        return result;
    }
}
