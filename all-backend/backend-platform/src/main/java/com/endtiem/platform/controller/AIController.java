/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubActivity;
import com.endtiem.club.entity.ClubTag;
import com.endtiem.club.mapper.ClubActivityMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.entity.AiRecommendation;
import com.endtiem.platform.entity.ClubAiSummary;
import com.endtiem.platform.entity.Tag;
import com.endtiem.platform.mapper.AiRecommendationMapper;
import com.endtiem.platform.mapper.ClubAiSummaryMapper;
import com.endtiem.platform.mapper.TagMapper;
import com.endtiem.recruitment.entity.UserTag;
import com.endtiem.recruitment.mapper.ClubMemberMapper;
import com.endtiem.recruitment.mapper.UserTagMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final ClubTagMapper clubTagMapper;
    private final ClubActivityMapper clubActivityMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final TagMapper tagMapper;
    private final AiRecommendationMapper aiRecommendationMapper;
    private final ClubAiSummaryMapper clubAiSummaryMapper;
    private final UserTagMapper userTagMapper;
    private final JwtUtil jwtUtil;

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.api.model:deepseek-chat}")
    private String model;

    public AIController(
            UserMapper userMapper,
            ClubMapper clubMapper,
            ClubTagMapper clubTagMapper,
            ClubActivityMapper clubActivityMapper,
            ClubMemberMapper clubMemberMapper,
            TagMapper tagMapper,
            AiRecommendationMapper aiRecommendationMapper,
            ClubAiSummaryMapper clubAiSummaryMapper,
            UserTagMapper userTagMapper,
            JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.clubMapper = clubMapper;
        this.clubTagMapper = clubTagMapper;
        this.clubActivityMapper = clubActivityMapper;
        this.clubMemberMapper = clubMemberMapper;
        this.tagMapper = tagMapper;
        this.aiRecommendationMapper = aiRecommendationMapper;
        this.clubAiSummaryMapper = clubAiSummaryMapper;
        this.userTagMapper = userTagMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/recommend")
    public Map<String, String> recommendClubs(@RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("未授权访问");
        }
        String token = auth.substring(7);

        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        User user = userMapper.selectById(userId);

        List<Integer> tagIds = parseIntegerList(request.get("tagIds"));
        String specialty = (String) request.get("specialty");
        String hobby = (String) request.get("hobby");
        String expectation = (String) request.get("expectation");

        List<Club> clubs = clubMapper.selectList(null);
        List<Tag> allTags = tagMapper.selectList(null);
        Map<Integer, Tag> tagById =
                allTags.stream().collect(Collectors.toMap(Tag::getId, t -> t, (a, b) -> a));

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个社团推荐专家。根据以下学生信息，推荐最适合的社团。\n\n");
        prompt.append("学生信息：\n");

        if (user != null) {
            prompt.append("- 学号：")
                    .append(user.getStudentId() != null ? user.getStudentId() : "")
                    .append("\n");
            prompt.append("- 姓名：")
                    .append(user.getRealName() != null ? user.getRealName() : "")
                    .append("\n");
        }

        if (specialty != null && !specialty.isEmpty()) {
            prompt.append("- 特长：").append(specialty).append("\n");
        }
        if (hobby != null && !hobby.isEmpty()) {
            prompt.append("- 爱好：").append(hobby).append("\n");
        }
        if (expectation != null && !expectation.isEmpty()) {
            prompt.append("- 期望收获：").append(expectation).append("\n");
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            prompt.append("- 感兴趣的标签：");
            for (Integer tagId : tagIds) {
                Tag tag = tagById.get(tagId);
                if (tag != null) {
                    prompt.append(tag.getName()).append("、");
                }
            }
            prompt.append("\n");
        }

        prompt.append("\n可选社团列表：\n");
        for (Club club : clubs) {
            List<ClubTag> clubTags = clubTagMapper.selectByClubId(club.getId());
            String tagNames =
                    clubTags.stream()
                            .map(ct -> tagById.get(ct.getTagId()))
                            .filter(Objects::nonNull)
                            .map(Tag::getName)
                            .collect(Collectors.joining("、"));

            prompt.append("- ").append(club.getName()).append("：").append(club.getDescription());
            if (!tagNames.isEmpty()) {
                prompt.append("（标签：").append(tagNames).append("）");
            }
            prompt.append("\n");
        }

        prompt.append("\n请根据学生的特长、爱好、期望和兴趣标签，推荐最适合的社团。");
        prompt.append("请用中文回答，格式如下：\n");
        prompt.append("**推荐社团：**\n");
        prompt.append("- 社团名称：推荐理由\n");
        prompt.append("\n**发展建议：**\n");
        prompt.append("给出一些加入社团后的发展建议\n");

        String recommendation =
                callAiApi(prompt.toString(), tagIds, clubs, tagById, specialty, hobby, expectation);

        saveRecommendations(userId, recommendation, clubs, tagIds);

        Map<String, String> result = new HashMap<>();
        result.put("recommendation", recommendation);
        return result;
    }

    @GetMapping("/club-summary/{clubId}")
    public Map<String, Object> getClubSummary(@PathVariable Integer clubId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        String summaryType = "monthly";
        ClubAiSummary summary = clubAiSummaryMapper.selectLatestByClubId(clubId);
        if (summary == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("clubId", clubId);
            result.put("clubName", club.getName());
            result.put("hasSummary", false);
            result.put("message", "暂无AI总结，请先生成");
            return result;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", summary.getId());
        result.put("clubId", summary.getClubId());
        result.put("clubName", club.getName());
        result.put("summaryType", summary.getSummaryType());
        result.put("summaryContent", summary.getSummaryContent());
        result.put("generatedAt", summary.getGeneratedAt());
        result.put("hasSummary", true);
        return result;
    }

    @PostMapping("/club-summary/{clubId}")
    public Map<String, Object> generateClubSummary(
            @PathVariable Integer clubId,
            @RequestParam(defaultValue = "monthly") String summaryType) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        String token = auth.substring(7);
        Integer role = jwtUtil.getRoleFromToken(token);
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);

        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        boolean isClubLeader = club.getAdminId() != null && club.getAdminId().equals(relatedId);
        boolean isPlatformAdmin = role != null && role >= 2;

        if (!isClubLeader && !isPlatformAdmin) {
            throw new RuntimeException("只有社团负责人或平台管理员可以生成AI总结");
        }

        List<ClubActivity> activities =
                clubActivityMapper.selectByClubIdOrderByCreatedAtDesc(clubId);
        long memberCount = clubMemberMapper.countActiveByClubId(clubId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = now;
        LocalDateTime periodStart;
        if ("monthly".equals(summaryType)) {
            periodStart = now.minusMonths(1);
        } else if ("quarterly".equals(summaryType)) {
            periodStart = now.minusMonths(3);
        } else {
            periodStart = now.minusYears(1);
        }

        List<ClubActivity> periodActivities =
                activities.stream()
                        .filter(
                                a ->
                                        a.getCreatedAt() != null
                                                && a.getCreatedAt().isAfter(periodStart)
                                                && a.getCreatedAt().isBefore(periodEnd))
                        .collect(Collectors.toList());

        String summaryContent =
                generateClubSummaryContent(club, memberCount, periodActivities, summaryType);

        ClubAiSummary summary = new ClubAiSummary();
        summary.setClubId(clubId);
        summary.setSummaryType(summaryType);
        summary.setSummaryContent(summaryContent);
        summary.setGeneratedAt(now);
        summary.setCreatedAt(now);
        summary.setUpdatedAt(now);
        clubAiSummaryMapper.insert(summary);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", summary.getId());
        result.put("clubId", clubId);
        result.put("clubName", club.getName());
        result.put("summaryType", summaryType);
        result.put("summaryContent", summaryContent);
        result.put("generatedAt", now);
        return result;
    }

    @PutMapping("/club-summary/{clubId}/refresh")
    public Map<String, Object> refreshClubSummary(
            @PathVariable Integer clubId,
            @RequestParam(defaultValue = "monthly") String summaryType) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        String token = auth.substring(7);
        Integer role = jwtUtil.getRoleFromToken(token);
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);

        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        boolean isClubLeader = club.getAdminId() != null && club.getAdminId().equals(relatedId);
        boolean isPlatformAdmin = role != null && role >= 2;

        if (!isClubLeader && !isPlatformAdmin) {
            throw new RuntimeException("只有社团负责人或平台管理员可以刷新AI总结");
        }

        ClubAiSummary existingSummary = clubAiSummaryMapper.selectLatestByClubId(clubId);
        if (existingSummary != null) {
            clubAiSummaryMapper.deleteById(existingSummary.getId());
        }

        return generateClubSummary(clubId, summaryType);
    }

    private String generateClubSummaryContent(
            Club club, long memberCount, List<ClubActivity> activities, String summaryType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个社团运营分析专家。请为以下社团生成一份")
                .append(getSummaryTypeName(summaryType))
                .append("总结。\n\n");
        prompt.append("社团信息：\n");
        prompt.append("- 社团名称：").append(club.getName()).append("\n");
        prompt.append("- 社团描述：")
                .append(club.getDescription() != null ? club.getDescription() : "暂无")
                .append("\n");

        List<ClubTag> clubTags = clubTagMapper.selectByClubId(club.getId());
        if (!clubTags.isEmpty()) {
            List<String> tagNames = new ArrayList<>();
            for (ClubTag ct : clubTags) {
                Tag tag = tagMapper.selectById(ct.getTagId());
                if (tag != null && tag.getName() != null) {
                    tagNames.add(tag.getName());
                }
            }
            if (!tagNames.isEmpty()) {
                prompt.append("- 社团标签：").append(String.join("、", tagNames)).append("\n");
            }
        }

        prompt.append("- 当前成员数：").append(memberCount).append("人\n");

        int approvedActivities =
                (int)
                        activities.stream()
                                .filter(a -> a.getStatus() != null && a.getStatus() == 1)
                                .count();
        prompt.append("- 本期举办活动数：").append(approvedActivities).append("场\n");

        if (!activities.isEmpty()) {
            prompt.append("- 近期活动：\n");
            for (int i = 0; i < Math.min(activities.size(), 5); i++) {
                ClubActivity activity = activities.get(i);
                if (activity.getStatus() == 1) {
                    prompt.append("  ").append(i + 1).append(". ").append(activity.getTitle());
                    if (activity.getActivityTime() != null) {
                        prompt.append("（")
                                .append(activity.getActivityTime().toLocalDate().toString())
                                .append("）");
                    }
                    prompt.append("\n");
                }
            }
        }

        prompt.append("\n请用中文生成一份简洁的总结报告，包括：\n");
        prompt.append("1. 本期社团运营概况\n");
        prompt.append("2. 活动亮点回顾\n");
        prompt.append("3. 发展建议\n\n");
        prompt.append("总结内容（Markdown格式）：\n");

        return callAiApiForSummary(prompt.toString());
    }

    private String getSummaryTypeName(String summaryType) {
        return switch (summaryType) {
            case "monthly" -> "月度";
            case "quarterly" -> "季度";
            case "annual" -> "年度";
            default -> "月度";
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String callAiApiForSummary(String prompt) {
        String key = apiKey == null ? "" : apiKey.trim();
        if (key.isEmpty()) {
            log.debug("跳过 DeepSeek：ai.api.key 为空");
            return generateLocalSummary(prompt);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(message));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1500);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(key);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> responseMessage =
                            (Map<String, Object>) choices.get(0).get("message");
                    if (responseMessage != null) {
                        Object content = responseMessage.get("content");
                        return content != null ? content.toString() : "";
                    }
                }
            }
        } catch (Exception e) {
            log.warn("DeepSeek 调用失败，已降级为本地总结: {}", e.getMessage());
            return generateLocalSummary(prompt);
        }

        return generateLocalSummary(prompt);
    }

    private String generateLocalSummary(String prompt) {
        StringBuilder result = new StringBuilder();
        result.append("**社团运营总结**\n\n");
        result.append("--- 运营概况 ---\n\n");
        result.append("本期社团运营情况良好，成员活跃度较高。\n\n");
        result.append("--- 活动回顾 ---\n\n");
        result.append("- 成功举办多场社团活动\n");
        result.append("- 活动内容丰富多样\n");
        result.append("- 吸引了众多同学参与\n\n");
        result.append("--- 发展建议 ---\n\n");
        result.append("1. 继续举办高质量的社团活动\n");
        result.append("2. 加强社团内部交流与协作\n");
        result.append("3. 积极招募新成员，扩大社团影响力\n");
        result.append("4. 鼓励成员参与活动策划与组织\n\n");
        result.append("--- 备注 ---\n\n");
        result.append("*当前为本地生成的简要总结，配置 AI API Key 后可生成更详细、智能的分析报告。*\n");
        return result.toString();
    }

    private static List<Integer> parseIntegerList(Object raw) {
        if (raw == null) {
            return null;
        }
        if (!(raw instanceof List<?> list)) {
            return null;
        }
        List<Integer> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Number) {
                out.add(((Number) o).intValue());
            } else if (o != null) {
                out.add(Integer.parseInt(o.toString()));
            }
        }
        return out;
    }

    private void saveRecommendations(
            Integer userId, String recommendation, List<Club> clubs, List<Integer> requestTagIds) {
        Set<String> recommendedClubNames = extractClubNames(recommendation, clubs);

        Set<Integer> userTagIds = new HashSet<>();
        if (requestTagIds != null) {
            for (Integer id : requestTagIds) {
                if (id != null) {
                    userTagIds.add(id);
                }
            }
        }
        for (UserTag ut : userTagMapper.selectByUserId(userId)) {
            userTagIds.add(ut.getTagId());
        }

        for (Club club : clubs) {
            if (recommendedClubNames.contains(club.getName())) {
                List<ClubTag> clubTags = clubTagMapper.selectByClubId(club.getId());
                Set<Integer> clubTagIds =
                        clubTags.stream().map(ClubTag::getTagId).collect(Collectors.toSet());

                double similarity = calculateJaccardSimilarity(userTagIds, clubTagIds);

                AiRecommendation existing =
                        aiRecommendationMapper.selectByUserIdAndClubId(userId, club.getId());
                if (existing == null) {
                    AiRecommendation aiRec = new AiRecommendation();
                    aiRec.setUserId(userId);
                    aiRec.setClubId(club.getId());
                    aiRec.setRecommendationText(recommendation);
                    aiRec.setSimilarity(similarity);
                    aiRec.setStatus("pending");
                    aiRec.setCreatedAt(LocalDateTime.now());
                    aiRecommendationMapper.insert(aiRec);
                } else {
                    existing.setRecommendationText(recommendation);
                    existing.setSimilarity(similarity);
                    existing.setStatus("pending");
                    aiRecommendationMapper.updateById(existing);
                }
            }
        }
    }

    private static double calculateJaccardSimilarity(
            Set<Integer> userTagIds, Set<Integer> clubTagIds) {
        if (userTagIds.isEmpty() && clubTagIds.isEmpty()) {
            return 0.0;
        }

        int intersection = 0;
        for (Integer tagId : userTagIds) {
            if (clubTagIds.contains(tagId)) {
                intersection++;
            }
        }

        Set<Integer> union = new HashSet<>(userTagIds);
        union.addAll(clubTagIds);
        int unionSize = union.size();

        return unionSize > 0 ? (double) intersection / unionSize : 0.0;
    }

    private static Set<String> extractClubNames(String recommendation, List<Club> clubs) {
        Set<String> clubNames = new HashSet<>();
        for (Club club : clubs) {
            if (club.getName() != null && recommendation.contains(club.getName())) {
                clubNames.add(club.getName());
            }
        }
        return clubNames;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String callAiApi(
            String prompt,
            List<Integer> interestTagIds,
            List<Club> clubs,
            Map<Integer, Tag> tagById,
            String specialty,
            String hobby,
            String expectation) {
        String key = apiKey == null ? "" : apiKey.trim();
        if (key.isEmpty()) {
            log.debug(
                    "跳过 DeepSeek：ai.api.key 为空。请设置环境变量 AI_API_KEY，或在运行目录放置 application-local.yml（见"
                            + " application.yml 的 spring.config.import）。");
            return generateLocalRecommendation(
                    interestTagIds, specialty, hobby, expectation, clubs, tagById);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(message));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(key);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> responseMessage =
                            (Map<String, Object>) choices.get(0).get("message");
                    if (responseMessage != null) {
                        Object content = responseMessage.get("content");
                        return content != null ? content.toString() : "";
                    }
                }
            }
        } catch (Exception e) {
            log.warn("DeepSeek 调用失败，已降级为本地标签推荐: {}", e.getMessage());
            return generateLocalRecommendation(
                    interestTagIds, specialty, hobby, expectation, clubs, tagById);
        }

        return generateLocalRecommendation(
                interestTagIds, specialty, hobby, expectation, clubs, tagById);
    }

    private String generateLocalRecommendation(
            List<Integer> interestTagIds,
            String specialty,
            String hobby,
            String expectation,
            List<Club> clubs,
            Map<Integer, Tag> tagById) {
        Set<Integer> interest = new HashSet<>();
        if (interestTagIds != null) {
            for (Integer id : interestTagIds) {
                if (id != null) {
                    interest.add(id);
                }
            }
        }

        List<ClubTagMatch> ranked = new ArrayList<>();
        for (Club club : clubs) {
            List<ClubTag> clubTags = clubTagMapper.selectByClubId(club.getId());
            List<String> matchedNames = new ArrayList<>();
            int matchCount = 0;
            for (ClubTag ct : clubTags) {
                if (Boolean.TRUE.equals(ct.getIsHidden())) {
                    continue;
                }
                if (interest.contains(ct.getTagId())) {
                    matchCount++;
                    Tag t = tagById.get(ct.getTagId());
                    if (t != null) {
                        matchedNames.add(t.getName());
                    }
                }
            }
            ranked.add(new ClubTagMatch(club, matchCount, matchedNames));
        }
        ranked.sort(
                (a, b) -> {
                    int c = Integer.compare(b.matchCount, a.matchCount);
                    if (c != 0) {
                        return c;
                    }
                    return Integer.compare(a.club.getId(), b.club.getId());
                });

        StringBuilder result = new StringBuilder();
        result.append("**推荐社团：**\n\n");

        final int maxShow = 5;
        int shown = 0;

        if (interest.isEmpty()) {
            result.append("*当前未选择兴趣标签，以下为系统中部分社团供参考。*\n");
            result.append("*（配置 ai.api.key 后可使用大模型生成更个性化的文案。）*\n\n");
            for (ClubTagMatch m : ranked) {
                if (shown >= 3) {
                    break;
                }
                appendLocalClubParagraph(result, m.club, tagById, 0, List.of());
                shown++;
            }
        } else {
            for (ClubTagMatch m : ranked) {
                if (shown >= maxShow) {
                    break;
                }
                if (m.matchCount > 0) {
                    appendLocalClubParagraph(result, m.club, tagById, m.matchCount, m.matchedNames);
                    shown++;
                }
            }
            if (shown == 0) {
                result.append("*暂无社团的公开标签与您选择的兴趣一致，以下为部分社团供浏览：*\n\n");
                for (ClubTagMatch m : ranked) {
                    if (shown >= 3) {
                        break;
                    }
                    appendLocalClubParagraph(result, m.club, tagById, 0, List.of());
                    shown++;
                }
            }
        }

        boolean hasExtra =
                (specialty != null && !specialty.isBlank())
                        || (hobby != null && !hobby.isBlank())
                        || (expectation != null && !expectation.isBlank());
        if (hasExtra) {
            result.append("\n*已结合您填写的特长/爱好/期望；本地推荐主要依据兴趣标签匹配度，完整语义解读可配置 AI 接口后由模型生成。*\n");
        }

        result.append("\n**发展建议：**\n\n");
        result.append("- 积极参与社团活动，结交志同道合的朋友\n");
        result.append("- 发挥自己的特长，为社团贡献力量\n");
        result.append("- 保持学习热情，不断提升自己的能力\n");
        result.append("- 平衡学习与社团活动，合理安排时间\n");

        return result.toString();
    }

    private void appendLocalClubParagraph(
            StringBuilder result,
            Club club,
            Map<Integer, Tag> tagById,
            int matchCount,
            List<String> matchedNames) {
        result.append("- **").append(club.getName()).append("**：");
        if (matchCount > 0 && !matchedNames.isEmpty()) {
            result.append("与您的兴趣标签「").append(String.join("」「", matchedNames)).append("」匹配；");
        }
        if (club.getDescription() != null && !club.getDescription().isBlank()) {
            result.append(club.getDescription());
        }
        result.append("\n");

        List<ClubTag> clubTags = clubTagMapper.selectByClubId(club.getId());
        String visible =
                clubTags.stream()
                        .filter(ct -> !Boolean.TRUE.equals(ct.getIsHidden()))
                        .map(ct -> tagById.get(ct.getTagId()))
                        .filter(Objects::nonNull)
                        .map(Tag::getName)
                        .collect(Collectors.joining("、"));
        if (!visible.isEmpty()) {
            result.append("  社团公开标签：").append(visible).append("\n");
        }
        result.append("\n");
    }

    private static final class ClubTagMatch {
        final Club club;
        final int matchCount;
        final List<String> matchedNames;

        ClubTagMatch(Club club, int matchCount, List<String> matchedNames) {
            this.club = club;
            this.matchCount = matchCount;
            this.matchedNames = matchedNames;
        }
    }
}
