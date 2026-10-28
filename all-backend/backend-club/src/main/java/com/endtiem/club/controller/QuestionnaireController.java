/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.controller;

import com.endtiem.club.entity.Questionnaire;
import com.endtiem.club.entity.QuestionnaireQuestion;
import com.endtiem.club.entity.QuestionnaireResponse;
import com.endtiem.club.mapper.QuestionnaireMapper;
import com.endtiem.club.mapper.QuestionnaireQuestionMapper;
import com.endtiem.club.mapper.QuestionnaireResponseMapper;
import com.endtiem.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
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
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionnaireQuestionMapper questionMapper;
    private final QuestionnaireResponseMapper responseMapper;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    public QuestionnaireController(
            QuestionnaireMapper questionnaireMapper,
            QuestionnaireQuestionMapper questionMapper,
            QuestionnaireResponseMapper responseMapper,
            JwtUtil jwtUtil,
            JdbcTemplate jdbcTemplate) {
        this.questionnaireMapper = questionnaireMapper;
        this.questionMapper = questionMapper;
        this.responseMapper = responseMapper;
        this.jwtUtil = jwtUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/club/{clubId}")
    public List<Map<String, Object>> getByClub(
            @PathVariable Integer clubId,
            @RequestParam(defaultValue = "false") boolean sortByMatch) {
        List<Questionnaire> questionnaires;
        if (clubId == null || clubId <= 0) {
            questionnaires = questionnaireMapper.selectActive();
        } else {
            questionnaires = questionnaireMapper.selectByClubId(clubId);
        }

        if (sortByMatch) {
            Integer userId = getUserIdOrNull();
            if (userId != null) {
                Set<Integer> userTagIds = getUserTagIds(userId);
                if (!userTagIds.isEmpty()) {
                    return questionnaires.stream()
                            .map(
                                    q -> {
                                        Set<Integer> qTagIds = parseTagsToIds(q.getTags());
                                        double similarity =
                                                calculateJaccardSimilarity(userTagIds, qTagIds);
                                        Map<String, Object> row = toDetailMap(q);
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

        return toSummaryList(questionnaires);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }
        List<QuestionnaireQuestion> questions = questionMapper.selectByQuestionnaireId(id);
        int responseCount = responseMapper.countByQuestionnaireId(id);
        Map<String, Object> result = toDetailMap(questionnaire);
        result.put("questions", questions);
        result.put("responseCount", responseCount);
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();
        Integer clubId = toInteger(request.get("clubId"));
        requireClubAdmin(clubId, userId);

        String title = (String) request.get("title");
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("问卷标题不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setClubId(clubId);
        questionnaire.setTitle(title.trim());
        questionnaire.setDescription((String) request.get("description"));
        questionnaire.setStartTime(parseDateTime(request.get("startTime")));
        questionnaire.setEndTime(parseDateTime(request.get("endTime")));
        questionnaire.setStatus(1);
        questionnaire.setAllowAnonymous(request.get("allowAnonymous") != null ? 1 : 0);
        questionnaire.setCreatedAt(now);
        questionnaire.setUpdatedAt(now);
        questionnaireMapper.insert(questionnaire);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questionsData =
                (List<Map<String, Object>>) request.get("questions");
        if (questionsData != null) {
            for (int i = 0; i < questionsData.size(); i++) {
                Map<String, Object> q = questionsData.get(i);
                QuestionnaireQuestion question = new QuestionnaireQuestion();
                question.setQuestionnaireId(questionnaire.getId());
                question.setQuestionText((String) q.get("questionText"));
                question.setQuestionType(toInteger(q.get("questionType")));
                question.setOptions(toJsonArray(q.get("options")));
                question.setRequired(q.get("required") != null ? 1 : 0);
                question.setSortOrder(i);
                questionMapper.insert(question);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", questionnaire.getId());
        result.put("title", questionnaire.getTitle());
        result.put("status", questionnaire.getStatus());
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(
            @PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }
        requireClubAdmin(questionnaire.getClubId(), userId);

        if (request.get("title") != null) {
            questionnaire.setTitle(request.get("title").toString().trim());
        }
        if (request.get("description") != null) {
            questionnaire.setDescription(request.get("description").toString());
        }
        if (request.get("startTime") != null) {
            questionnaire.setStartTime(parseDateTime(request.get("startTime")));
        }
        if (request.get("endTime") != null) {
            questionnaire.setEndTime(parseDateTime(request.get("endTime")));
        }
        if (request.get("status") != null) {
            questionnaire.setStatus(toInteger(request.get("status")));
        }
        questionnaire.setUpdatedAt(LocalDateTime.now());
        questionnaireMapper.updateById(questionnaire);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", id);
        return result;
    }

    @PostMapping("/{id}/submit")
    public Map<String, Object> submit(
            @PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer userId = getUserIdOrNull();
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }
        if (questionnaire.getStatus() != 1) {
            throw new RuntimeException("问卷未开放");
        }
        if (userId != null) {
            QuestionnaireResponse existing =
                    responseMapper.selectByQuestionnaireIdAndUserId(id, userId);
            if (existing != null) {
                throw new RuntimeException("您已提交过问卷");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setQuestionnaireId(id);
        response.setUserId(userId);
        if (userId == null && questionnaire.getAllowAnonymous() == 1) {
            response.setAnonymousName((String) request.get("anonymousName"));
        }
        response.setAnswers(toJsonString(request.get("answers")));
        response.setSubmittedAt(now);
        responseMapper.insert(response);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "提交成功");
        return result;
    }

    @GetMapping("/{id}/responses")
    public List<Map<String, Object>> getResponses(@PathVariable Integer id) {
        Integer userId = requireUserIdFromToken();
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }
        requireClubAdmin(questionnaire.getClubId(), userId);

        List<QuestionnaireResponse> responses = responseMapper.selectByQuestionnaireId(id);
        List<Map<String, Object>> result = new ArrayList<>();
        for (QuestionnaireResponse r : responses) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("userId", r.getUserId());
            row.put("anonymousName", r.getAnonymousName());
            row.put("answers", parseJson(r.getAnswers()));
            row.put("submittedAt", r.getSubmittedAt());
            result.add(row);
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        Integer userId = requireUserIdFromToken();
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }
        requireClubAdmin(questionnaire.getClubId(), userId);
        questionnaireMapper.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private List<Map<String, Object>> toSummaryList(List<Questionnaire> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Questionnaire q : list) {
            result.add(toDetailMap(q));
        }
        return result;
    }

    private Map<String, Object> toDetailMap(Questionnaire q) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", q.getId());
        row.put("clubId", q.getClubId());
        row.put("title", q.getTitle());
        row.put("description", q.getDescription());
        row.put("startTime", q.getStartTime());
        row.put("endTime", q.getEndTime());
        row.put("status", q.getStatus());
        row.put("allowAnonymous", q.getAllowAnonymous());
        row.put("tags", q.getTags());
        row.put("createdAt", q.getCreatedAt());
        row.put("updatedAt", q.getUpdatedAt());
        return row;
    }

    private void requireClubAdmin(Integer clubId, Integer userId) {
        Integer role = getRoleFromToken();
        if (role != null && role >= 2) {
            Integer managedClubId = role - 1;
            if (managedClubId.equals(clubId)) {
                return;
            }
        }
        if (role != null && role == 1) {
            return;
        }
        throw new RuntimeException("只有社团管理员可以操作");
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

    private Integer getUserIdOrNull() {
        try {
            HttpServletRequest httpRequest =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                            .getRequest();
            String auth = httpRequest.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                return null;
            }
            String token = auth.substring(7);
            return jwtUtil.getRelatedIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getRoleFromToken() {
        try {
            HttpServletRequest httpRequest =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                            .getRequest();
            String auth = httpRequest.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                return null;
            }
            String token = auth.substring(7);
            return jwtUtil.getRoleFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime) return (LocalDateTime) o;
        if (o instanceof String) {
            try {
                return LocalDateTime.parse((String) o);
            } catch (Exception e) {
                return null;
            }
        }
        if (o instanceof Number) {
            long ms = ((Number) o).longValue();
            return java.time.Instant.ofEpochMilli(ms)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return null;
    }

    private static String toJsonArray(Object o) {
        if (o == null) return null;
        if (o instanceof List) {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(o).toString();
            } catch (Exception e) {
                return null;
            }
        }
        return o.toString();
    }

    private static String toJsonString(Object o) {
        if (o == null) return null;
        if (o instanceof String) return (String) o;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(o).toString();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> parseJson(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Set<Integer> getUserTagIds(int userId) {
        List<Integer> tagIds =
                jdbcTemplate.queryForList(
                        "SELECT tag_id FROM user_tag WHERE user_id = ?", Integer.class, userId);
        return new HashSet<>(tagIds);
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
