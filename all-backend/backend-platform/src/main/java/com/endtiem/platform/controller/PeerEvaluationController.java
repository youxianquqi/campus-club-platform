/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.dto.PeerEvaluationRequest;
import com.endtiem.platform.entity.PeerEvaluation;
import com.endtiem.platform.mapper.PeerEvaluationMapper;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/evaluations")
public class PeerEvaluationController {

    private final PeerEvaluationMapper evaluationMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public PeerEvaluationController(
            PeerEvaluationMapper evaluationMapper, UserMapper userMapper, JwtUtil jwtUtil) {
        this.evaluationMapper = evaluationMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/neighbors")
    public List<Map<String, Object>> getNeighboringStudents() {
        Integer currentUserId = getCurrentUserId();
        List<User> neighbors = userMapper.selectNeighboringStudents(currentUserId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (User st : neighbors) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", st.getId());
            userInfo.put("studentId", st.getStudentId());
            userInfo.put("name", st.getRealName());
            userInfo.put(
                    "evaluated",
                    evaluationMapper.existsByEvaluatorIdAndEvaluatedId(currentUserId, st.getId()));
            result.add(userInfo);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> submitEvaluation(@RequestBody PeerEvaluationRequest request) {
        Integer evaluatorId = getCurrentUserId();
        Integer evaluatedId = request.getEvaluatedId();

        Map<String, Object> response = new HashMap<>();

        if (evaluatorId.equals(evaluatedId)) {
            response.put("success", false);
            response.put("message", "不能评价自己");
            return response;
        }

        if (evaluationMapper.existsByEvaluatorIdAndEvaluatedId(evaluatorId, evaluatedId)) {
            response.put("success", false);
            response.put("message", "您已经评价过该同学");
            return response;
        }

        if (!"其他".equals(request.getActivityType())
                && request.getCustomActivity() != null
                && !request.getCustomActivity().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "只有选择'其他'时才能填写自定义内容");
            return response;
        }

        PeerEvaluation evaluation = new PeerEvaluation();
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setEvaluatedId(evaluatedId);
        evaluation.setActivityType(request.getActivityType());
        evaluation.setCustomActivity(request.getCustomActivity());
        evaluation.setRating(request.getRating() != null ? request.getRating() : 5);
        evaluation.setComment(request.getComment());
        evaluation.setCreatedAt(LocalDateTime.now());

        evaluationMapper.insert(evaluation);

        response.put("success", true);
        response.put("message", "评价提交成功");
        return response;
    }

    @GetMapping("/my-evaluations")
    public List<Map<String, Object>> getMyEvaluations() {
        Integer currentUserId = getCurrentUserId();
        List<PeerEvaluation> evaluations = evaluationMapper.selectByEvaluatorId(currentUserId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (PeerEvaluation evaluation : evaluations) {
            User evaluated = userMapper.selectById(evaluation.getEvaluatedId());
            Map<String, Object> evalInfo = new HashMap<>();
            evalInfo.put("id", evaluation.getId());
            evalInfo.put("evaluatedName", evaluated != null ? evaluated.getRealName() : "");
            evalInfo.put("evaluatedStudentId", evaluated != null ? evaluated.getStudentId() : null);
            evalInfo.put("activityType", evaluation.getActivityType());
            evalInfo.put("customActivity", evaluation.getCustomActivity());
            evalInfo.put("rating", evaluation.getRating());
            evalInfo.put("comment", evaluation.getComment());
            evalInfo.put("createdAt", evaluation.getCreatedAt());
            result.add(evalInfo);
        }
        return result;
    }

    @GetMapping("/received")
    public Map<String, Object> getReceivedEvaluations() {
        Integer currentUserId = getCurrentUserId();
        List<PeerEvaluation> evaluations = evaluationMapper.selectByEvaluatedId(currentUserId);

        List<Map<String, Object>> evalList = new ArrayList<>();
        for (PeerEvaluation evaluation : evaluations) {
            User evaluator = userMapper.selectById(evaluation.getEvaluatorId());
            Map<String, Object> evalInfo = new HashMap<>();
            evalInfo.put("id", evaluation.getId());
            evalInfo.put("evaluatorName", evaluator != null ? evaluator.getRealName() : "");
            evalInfo.put("activityType", evaluation.getActivityType());
            evalInfo.put("customActivity", evaluation.getCustomActivity());
            evalInfo.put("rating", evaluation.getRating());
            evalInfo.put("comment", evaluation.getComment());
            evalInfo.put("createdAt", evaluation.getCreatedAt());
            evalList.add(evalInfo);
        }

        Double avgRating = evaluationMapper.getAverageRatingForEvaluated(currentUserId);
        Integer totalCount = evaluationMapper.countEvaluationsForEvaluated(currentUserId);

        List<Map<String, Object>> activityCounts =
                evaluationMapper.countByActivityTypeForEvaluated(currentUserId);
        Map<String, Integer> activityStats = new HashMap<>();
        for (Map<String, Object> row : activityCounts) {
            Object typeObj = row.get("activityType");
            Object cntObj = row.get("cnt");
            if (typeObj != null && cntObj instanceof Number) {
                activityStats.put(typeObj.toString(), ((Number) cntObj).intValue());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("evaluations", evalList);
        result.put("averageRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        result.put("totalCount", totalCount != null ? totalCount : 0);
        result.put("activityStats", activityStats);

        return result;
    }

    private Integer getCurrentUserId() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getRelatedIdFromToken(token);
    }
}
