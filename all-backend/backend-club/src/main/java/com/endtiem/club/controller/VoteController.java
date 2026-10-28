/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.controller;

import com.endtiem.club.entity.Vote;
import com.endtiem.club.entity.VoteOption;
import com.endtiem.club.entity.VoteRecord;
import com.endtiem.club.mapper.VoteMapper;
import com.endtiem.club.mapper.VoteOptionMapper;
import com.endtiem.club.mapper.VoteRecordMapper;
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
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteMapper voteMapper;
    private final VoteOptionMapper voteOptionMapper;
    private final VoteRecordMapper voteRecordMapper;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    public VoteController(
            VoteMapper voteMapper,
            VoteOptionMapper voteOptionMapper,
            VoteRecordMapper voteRecordMapper,
            JwtUtil jwtUtil,
            JdbcTemplate jdbcTemplate) {
        this.voteMapper = voteMapper;
        this.voteOptionMapper = voteOptionMapper;
        this.voteRecordMapper = voteRecordMapper;
        this.jwtUtil = jwtUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/club/{clubId}")
    public List<Map<String, Object>> getByClub(
            @PathVariable Integer clubId,
            @RequestParam(defaultValue = "false") boolean sortByMatch) {
        List<Vote> votes;
        if (clubId == null || clubId <= 0) {
            votes = voteMapper.selectActive();
        } else {
            votes = voteMapper.selectByClubId(clubId);
        }

        if (sortByMatch) {
            Integer userId = getUserIdOrNull();
            if (userId != null) {
                Set<Integer> userTagIds = getUserTagIds(userId);
                if (!userTagIds.isEmpty()) {
                    return votes.stream()
                            .map(
                                    vote -> {
                                        Set<Integer> voteTagIds = parseTagsToIds(vote.getTags());
                                        double similarity =
                                                calculateJaccardSimilarity(userTagIds, voteTagIds);
                                        Map<String, Object> row = toDetailMap(vote);
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

        return toSummaryList(votes);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Vote vote = voteMapper.selectById(id);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        List<VoteOption> options = voteOptionMapper.selectByVoteId(id);
        int voteCount = voteRecordMapper.countByVoteId(id);

        Integer userId = getUserIdOrNull();
        boolean hasVoted = false;
        VoteRecord myRecord = null;
        if (userId != null) {
            myRecord = voteRecordMapper.selectByVoteIdAndUserId(id, userId);
            hasVoted = myRecord != null;
        }

        Map<String, Object> result = toDetailMap(vote);
        result.put("options", options);
        result.put("voteCount", voteCount);
        result.put("hasVoted", hasVoted);
        if (hasVoted && myRecord != null) {
            result.put("mySelectedOptions", parseJsonList(myRecord.getSelectedOptions()));
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();
        Integer clubId = toInteger(request.get("clubId"));
        requireClubAdmin(clubId, userId);

        String title = (String) request.get("title");
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("投票标题不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Vote vote = new Vote();
        vote.setClubId(clubId);
        vote.setTitle(title.trim());
        vote.setDescription((String) request.get("description"));
        vote.setVoteType(
                toInteger(request.get("voteType")) != null
                        ? toInteger(request.get("voteType"))
                        : 1);
        vote.setMaxOptions(
                toInteger(request.get("maxOptions")) != null
                        ? toInteger(request.get("maxOptions"))
                        : 1);
        vote.setStartTime(parseDateTime(request.get("startTime")));
        vote.setEndTime(parseDateTime(request.get("endTime")));
        vote.setStatus(1);
        vote.setAllowAnonymous(toInteger(request.get("allowAnonymous")) != null ? 1 : 0);
        vote.setShowResults(toInteger(request.get("showResults")) != null ? 1 : 1);
        vote.setTags((String) request.get("tags"));
        vote.setCreatedAt(now);
        vote.setUpdatedAt(now);
        voteMapper.insert(vote);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> optionsData = (List<Map<String, Object>>) request.get("options");
        if (optionsData != null) {
            for (int i = 0; i < optionsData.size(); i++) {
                Map<String, Object> o = optionsData.get(i);
                VoteOption option = new VoteOption();
                option.setVoteId(vote.getId());
                option.setOptionText((String) o.get("optionText"));
                option.setImageUrl((String) o.get("imageUrl"));
                option.setVoteCount(0);
                option.setSortOrder(i);
                voteOptionMapper.insert(option);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", vote.getId());
        result.put("title", vote.getTitle());
        result.put("status", vote.getStatus());
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(
            @PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();
        Vote vote = voteMapper.selectById(id);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        requireClubAdmin(vote.getClubId(), userId);

        if (request.get("title") != null) {
            vote.setTitle(request.get("title").toString().trim());
        }
        if (request.get("description") != null) {
            vote.setDescription(request.get("description").toString());
        }
        if (request.get("status") != null) {
            vote.setStatus(toInteger(request.get("status")));
        }
        vote.setUpdatedAt(LocalDateTime.now());
        voteMapper.updateById(vote);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", id);
        return result;
    }

    @PostMapping("/{id}/submit")
    public Map<String, Object> submit(
            @PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer userId = getUserIdOrNull();
        Vote vote = voteMapper.selectById(id);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        if (vote.getStatus() != 1) {
            throw new RuntimeException("投票未开放");
        }
        if (userId != null) {
            VoteRecord existing = voteRecordMapper.selectByVoteIdAndUserId(id, userId);
            if (existing != null) {
                throw new RuntimeException("您已投过票");
            }
        }

        @SuppressWarnings("unchecked")
        List<Integer> selectedOptions = (List<Integer>) request.get("selectedOptions");
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            throw new RuntimeException("请选择选项");
        }
        if (vote.getMaxOptions() != null && selectedOptions.size() > vote.getMaxOptions()) {
            throw new RuntimeException("最多只能选择" + vote.getMaxOptions() + "个选项");
        }

        LocalDateTime now = LocalDateTime.now();
        VoteRecord record = new VoteRecord();
        record.setVoteId(id);
        record.setUserId(userId);
        if (userId == null && vote.getAllowAnonymous() == 1) {
            record.setAnonymousName((String) request.get("anonymousName"));
        }
        record.setSelectedOptions(toJsonList(selectedOptions));
        record.setVotedAt(now);
        voteRecordMapper.insert(record);

        for (Integer optionId : selectedOptions) {
            VoteOption option = voteOptionMapper.selectById(optionId);
            if (option != null && option.getVoteId().equals(id)) {
                option.setVoteCount(option.getVoteCount() + 1);
                voteOptionMapper.updateById(option);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "投票成功");
        return result;
    }

    @GetMapping("/{id}/results")
    public Map<String, Object> getResults(@PathVariable Integer id) {
        Vote vote = voteMapper.selectById(id);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        List<VoteOption> options = voteOptionMapper.selectByVoteId(id);
        int voteCount = voteRecordMapper.countByVoteId(id);

        Map<String, Object> result = toDetailMap(vote);
        List<Map<String, Object>> optionsResult = new ArrayList<>();
        for (VoteOption option : options) {
            Map<String, Object> optionRow = new HashMap<>();
            optionRow.put("id", option.getId());
            optionRow.put("optionText", option.getOptionText());
            optionRow.put("imageUrl", option.getImageUrl());
            optionRow.put("voteCount", option.getVoteCount());
            optionRow.put(
                    "percentage", voteCount > 0 ? (option.getVoteCount() * 100.0 / voteCount) : 0);
            optionsResult.add(optionRow);
        }
        result.put("options", optionsResult);
        result.put("voteCount", voteCount);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        Integer userId = requireUserIdFromToken();
        Vote vote = voteMapper.selectById(id);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        requireClubAdmin(vote.getClubId(), userId);
        voteMapper.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private List<Map<String, Object>> toSummaryList(List<Vote> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Vote v : list) {
            result.add(toDetailMap(v));
        }
        return result;
    }

    private Map<String, Object> toDetailMap(Vote v) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", v.getId());
        row.put("clubId", v.getClubId());
        row.put("title", v.getTitle());
        row.put("description", v.getDescription());
        row.put("voteType", v.getVoteType());
        row.put("maxOptions", v.getMaxOptions());
        row.put("startTime", v.getStartTime());
        row.put("endTime", v.getEndTime());
        row.put("status", v.getStatus());
        row.put("allowAnonymous", v.getAllowAnonymous());
        row.put("showResults", v.getShowResults());
        row.put("tags", v.getTags());
        row.put("createdAt", v.getCreatedAt());
        row.put("updatedAt", v.getUpdatedAt());
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

    private static String toJsonList(List<Integer> list) {
        if (list == null) return "[]";
        return new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(list).toString();
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> parseJsonList(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            List<?> list =
                    new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, List.class);
            List<Integer> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Number) {
                    result.add(((Number) item).intValue());
                }
            }
            return result;
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
