/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubQuestion;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.ClubQuestionMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.mapper.ClubMemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/questions")
public class ClubQuestionController {

    private final ClubQuestionMapper questionMapper;
    private final ClubMapper clubMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public ClubQuestionController(
            ClubQuestionMapper questionMapper,
            ClubMapper clubMapper,
            ClubMemberMapper clubMemberMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil) {
        this.questionMapper = questionMapper;
        this.clubMapper = clubMapper;
        this.clubMemberMapper = clubMemberMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/club/{clubId}")
    public Map<String, Object> getQuestionsByClub(
            @PathVariable Integer clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ClubQuestion> questions =
                questionMapper.pageByClubIdOrderByCreatedAtDesc(
                        clubId, new Page<>(page + 1L, size));

        List<Map<String, Object>> questionList = new ArrayList<>();
        for (ClubQuestion q : questions.getRecords()) {
            questionList.add(convertToMap(q));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("questions", questionList);
        result.put("total", questions.getTotal());
        result.put("pages", questions.getPages());
        return result;
    }

    @PostMapping
    public Map<String, Object> createQuestion(@RequestBody Map<String, Object> request) {
        Integer userId = getCurrentUserId();
        Integer clubId = toInteger(request.get("clubId"));
        String content = (String) request.get("content");

        Map<String, Object> response = new HashMap<>();

        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "提问内容不能为空");
            return response;
        }

        if (clubMapper.selectById(clubId) == null) {
            response.put("success", false);
            response.put("message", "社团不存在");
            return response;
        }

        ClubQuestion question = new ClubQuestion();
        question.setClubId(clubId);
        question.setUserId(userId);
        question.setContent(content);
        question.setCreatedAt(LocalDateTime.now());

        questionMapper.insert(question);

        response.put("success", true);
        response.put("message", "提问成功");
        response.put("questionId", question.getId());
        return response;
    }

    @PostMapping("/{questionId}/answer")
    public Map<String, Object> answerQuestion(
            @PathVariable Integer questionId, @RequestBody Map<String, String> request) {
        Integer userId = getCurrentUserId();
        String answer = request.get("answer");

        Map<String, Object> response = new HashMap<>();

        if (answer == null || answer.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "回答内容不能为空");
            return response;
        }

        ClubQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            response.put("success", false);
            response.put("message", "提问不存在");
            return response;
        }

        Integer clubId = question.getClubId();

        if (!clubMemberMapper.existsByClubIdAndUserId(clubId.longValue(), userId.longValue())) {
            response.put("success", false);
            response.put("message", "只有该社团成员才能回答问题");
            return response;
        }

        question.setAnswer(answer);
        question.setAnsweredAt(LocalDateTime.now());
        questionMapper.updateById(question);

        response.put("success", true);
        response.put("message", "回答成功");
        return response;
    }

    @GetMapping("/my")
    public List<Map<String, Object>> getMyQuestions() {
        Integer userId = getCurrentUserId();
        List<ClubQuestion> questions = questionMapper.selectByUserIdOrderByCreatedAtDesc(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ClubQuestion q : questions) {
            result.add(convertToMap(q));
        }
        return result;
    }

    private Map<String, Object> convertToMap(ClubQuestion q) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", q.getId());
        map.put("clubId", q.getClubId());
        map.put("clubName", resolveClubName(q.getClubId()));
        map.put("userId", q.getUserId());
        User u = q.getUserId() != null ? userMapper.selectById(q.getUserId()) : null;
        map.put("studentName", u != null ? u.getRealName() : "匿名");
        map.put("studentAvatar", u != null ? u.getAvatar() : null);
        map.put("content", q.getContent());
        map.put("answer", q.getAnswer());
        map.put("answeredAt", q.getAnsweredAt());
        map.put("createdAt", q.getCreatedAt());
        map.put("isAnswered", q.getAnswer() != null && !q.getAnswer().isEmpty());
        return map;
    }

    private String resolveClubName(Integer clubId) {
        if (clubId == null) {
            return "未知社团";
        }
        Club club = clubMapper.selectById(clubId);
        return club != null ? club.getName() : "未知社团";
    }

    private Integer getCurrentUserId() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getRelatedIdFromToken(token);
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
}
