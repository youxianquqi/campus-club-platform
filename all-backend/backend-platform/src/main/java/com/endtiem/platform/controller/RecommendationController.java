/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.dto.ActivityRecommendation;
import com.endtiem.platform.dto.ClubRecommendation;
import com.endtiem.platform.dto.QuestionnaireRecommendation;
import com.endtiem.platform.dto.RecruitmentRecommendation;
import com.endtiem.platform.dto.UniversalRecommendation;
import com.endtiem.platform.dto.VoteRecommendation;
import com.endtiem.platform.service.RecommendationService;
import com.endtiem.platform.service.UniversalRecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UniversalRecommendationService universalRecommendationService;
    private final JwtUtil jwtUtil;

    public RecommendationController(
            RecommendationService recommendationService,
            UniversalRecommendationService universalRecommendationService,
            JwtUtil jwtUtil) {
        this.recommendationService = recommendationService;
        this.universalRecommendationService = universalRecommendationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Map<String, Object> getRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String qqBoost,
            @RequestParam(required = false) Integer qqBoostClubId) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        String token = auth.substring(7).trim();

        int safeSize = Math.min(Math.max(size, 1), 50);
        int p = Math.max(page, 0);

        Integer studentId = jwtUtil.getPrimaryUserIdFromToken(token);
        String qqKw = (qqBoost != null && !qqBoost.trim().isEmpty()) ? qqBoost.trim() : null;

        if (studentId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("clubs", new ArrayList<>());
            empty.put("total", 0L);
            empty.put("pages", 0);
            empty.put("currentPage", p);
            return empty;
        }

        List<ClubRecommendation> all =
                recommendationService.recommendForStudent(
                        studentId.longValue(), qqKw, qqBoostClubId);
        int from = p * safeSize;
        int total = all.size();
        int totalPages = safeSize > 0 ? (int) Math.ceil(total / (double) safeSize) : 0;
        List<ClubRecommendation> slice = new ArrayList<>();
        if (from < total) {
            int to = Math.min(from + safeSize, total);
            slice = all.subList(from, to);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("clubs", slice);
        response.put("total", (long) total);
        response.put("pages", totalPages);
        response.put("currentPage", p);
        return response;
    }

    @GetMapping("/activities")
    public Map<String, Object> getActivityRecommendations(
            @RequestParam(defaultValue = "8") int limit) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return emptyResponse("activities");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<ActivityRecommendation> activities =
                universalRecommendationService.recommendActivities(userId, safeLimit);

        Map<String, Object> response = new HashMap<>();
        response.put("activities", activities);
        response.put("total", (long) activities.size());
        return response;
    }

    @GetMapping("/votes")
    public Map<String, Object> getVoteRecommendations(@RequestParam(defaultValue = "8") int limit) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return emptyResponse("votes");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<VoteRecommendation> votes =
                universalRecommendationService.recommendVotes(userId, safeLimit);

        Map<String, Object> response = new HashMap<>();
        response.put("votes", votes);
        response.put("total", (long) votes.size());
        return response;
    }

    @GetMapping("/recruitments")
    public Map<String, Object> getRecruitmentRecommendations(
            @RequestParam(defaultValue = "8") int limit) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return emptyResponse("recruitments");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<RecruitmentRecommendation> recruitments =
                universalRecommendationService.recommendRecruitments(userId, safeLimit);

        Map<String, Object> response = new HashMap<>();
        response.put("recruitments", recruitments);
        response.put("total", (long) recruitments.size());
        return response;
    }

    @GetMapping("/questionnaires")
    public Map<String, Object> getQuestionnaireRecommendations(
            @RequestParam(defaultValue = "8") int limit) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return emptyResponse("questionnaires");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<QuestionnaireRecommendation> questionnaires =
                universalRecommendationService.recommendQuestionnaires(userId, safeLimit);

        Map<String, Object> response = new HashMap<>();
        response.put("questionnaires", questionnaires);
        response.put("total", (long) questionnaires.size());
        return response;
    }

    @GetMapping("/all")
    public Map<String, Object> getAllRecommendations(@RequestParam(defaultValue = "10") int limit) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return emptyResponse("all");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<UniversalRecommendation> all =
                universalRecommendationService.recommendAll(userId, safeLimit);

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", all);
        response.put("total", (long) all.size());
        return response;
    }

    private Integer getCurrentUserId() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        String token = auth.substring(7).trim();
        return jwtUtil.getPrimaryUserIdFromToken(token);
    }

    private Map<String, Object> emptyResponse(String type) {
        Map<String, Object> empty = new HashMap<>();
        switch (type) {
            case "activities":
                empty.put("activities", new ArrayList<>());
                break;
            case "votes":
                empty.put("votes", new ArrayList<>());
                break;
            case "recruitments":
                empty.put("recruitments", new ArrayList<>());
                break;
            case "questionnaires":
                empty.put("questionnaires", new ArrayList<>());
                break;
            case "all":
            default:
                empty.put("recommendations", new ArrayList<>());
                break;
        }
        empty.put("total", 0L);
        return empty;
    }
}
