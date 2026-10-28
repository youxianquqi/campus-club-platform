/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.dto.ClubRequest;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubTag;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.common.api.Result;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.entity.AiRecommendation;
import com.endtiem.platform.mapper.AiRecommendationMapper;
import com.endtiem.platform.mapper.TagMapper;
import com.endtiem.platform.service.ClubInterMemberMatchService;
import com.endtiem.recruitment.mapper.MembershipApplicationMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/clubs")
public class ClubController {

    private static final Logger log = LoggerFactory.getLogger(ClubController.class);

    private final ClubMapper clubMapper;
    private final ClubTagMapper clubTagMapper;
    private final JwtUtil jwtUtil;
    private final AiRecommendationMapper aiRecommendationMapper;
    private final MembershipApplicationMapper applicationMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final ClubInterMemberMatchService clubInterMemberMatchService;

    public ClubController(
            ClubMapper clubMapper,
            ClubTagMapper clubTagMapper,
            JwtUtil jwtUtil,
            AiRecommendationMapper aiRecommendationMapper,
            MembershipApplicationMapper applicationMapper,
            UserMapper userMapper,
            TagMapper tagMapper,
            ClubInterMemberMatchService clubInterMemberMatchService) {
        this.clubMapper = clubMapper;
        this.clubTagMapper = clubTagMapper;
        this.jwtUtil = jwtUtil;
        this.aiRecommendationMapper = aiRecommendationMapper;
        this.applicationMapper = applicationMapper;
        this.userMapper = userMapper;
        this.tagMapper = tagMapper;
        this.clubInterMemberMatchService = clubInterMemberMatchService;
    }

    @GetMapping
    public Map<String, Object> getClubs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(required = false) List<Integer> tagIds) {
        int currentPage = page < 1 ? 1 : page;

        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        Integer filterTagId = resolveFilterTagId(typeId, tagIds);
        if (filterTagId != null) {
            List<ClubTag> rels =
                    clubTagMapper.selectList(
                            new LambdaQueryWrapper<ClubTag>().eq(ClubTag::getTagId, filterTagId));
            List<Integer> clubIds =
                    rels.stream().map(ClubTag::getClubId).distinct().collect(Collectors.toList());
            if (clubIds.isEmpty()) {
                wrapper.eq(Club::getId, -1);
            } else {
                wrapper.in(Club::getId, clubIds);
            }
        }

        Page<Club> clubs = clubMapper.selectPage(new Page<>(currentPage, size), wrapper);

        List<Map<String, Object>> clubList = new ArrayList<>();
        for (Club club : clubs.getRecords()) {
            clubList.add(buildClubSummaryMap(club));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", clubList);
        data.put("total", clubs.getTotal());
        data.put("pages", clubs.getPages());
        data.put("currentPage", currentPage);

        return okEnvelope(data);
    }

    private static Integer resolveFilterTagId(Integer typeId, List<Integer> tagIds) {
        if (typeId != null) {
            return typeId;
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            return tagIds.get(0);
        }
        return null;
    }

    private static Map<String, Object> okEnvelope(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", data);
        return body;
    }

    private Map<String, Object> buildClubSummaryMap(Club club) {
        Map<String, Object> clubMap = new HashMap<>();
        clubMap.put("id", club.getId());
        clubMap.put("name", club.getName());
        clubMap.put("description", club.getDescription());
        clubMap.put("qqGroup", club.getQqGroup());
        clubMap.put("logoUrl", club.getLogoUrl());
        clubMap.put("adminId", club.getAdminId());
        clubMap.put("createdAt", club.getCreatedAt());

        if (club.getAdminId() != null && club.getAdminId() > 0) {
            User admin = userMapper.selectById(club.getAdminId());
            if (admin != null) {
                String n = admin.getRealName();
                clubMap.put("adminName", n != null && !n.isBlank() ? n : admin.getUsername());
            } else {
                clubMap.put("adminName", "未指定");
            }
        } else {
            clubMap.put("adminName", "未指定");
        }

        long memberCount = applicationMapper.countByClubIdAndStatus(club.getId(), 1);
        clubMap.put("memberCount", memberCount);

        List<ClubTag> clubTags = clubTagMapper.selectByClubId(club.getId());
        List<Integer> tagIdList = new ArrayList<>();
        for (ClubTag clubTag : clubTags) {
            tagIdList.add(clubTag.getTagId());
        }
        clubMap.put("tags", tagIdList);
        clubMap.put("cover", club.getLogoUrl());
        clubMap.put("logo", club.getLogoUrl());
        clubMap.put("leaderId", club.getAdminId());
        clubMap.put("typeId", tagIdList.isEmpty() ? null : tagIdList.get(0));
        clubMap.put("rating", null);
        return clubMap;
    }

    @GetMapping("/{id:\\d+}")
    public Map<String, Object> getClubById(@PathVariable Integer id) {
        Club club = clubMapper.selectById(id);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }
        return okEnvelope(buildClubSummaryMap(club));
    }

    @GetMapping("/my")
    public Map<String, Object> getMyClub() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role < 2) {
            throw new RuntimeException("您不是社团管理员");
        }

        Integer clubId = role - 1;
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new RuntimeException("您管理的社团不存在");
        }

        return okEnvelope(buildClubSummaryMap(club));
    }

    @PostMapping
    public Club createClub(@RequestBody ClubRequest request) {
        String token = requireBearerToken();
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);
        /* adminId 存 user.id；未登录学生主体时置 0，负责人展示为「未指定」 */
        Integer adminId = relatedId != null && relatedId > 0 ? relatedId : 0;

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("社团名称不能为空");
        }

        if (relatedId != null && relatedId > 0) {
            Club existingClub = clubMapper.selectByAdminId(relatedId);
            if (existingClub != null) {
                throw new RuntimeException("您已经创建过社团，每人只能创建一个社团");
            }
        }

        String description = buildCreateDescription(request);

        Club club = new Club();
        club.setName(request.getName().trim());
        club.setDescription(description);
        club.setQqGroup(request.getQqGroup());
        if (request.getLogoUrl() != null && !request.getLogoUrl().isBlank()) {
            club.setLogoUrl(request.getLogoUrl().trim());
        }
        club.setAdminId(adminId);
        club.setCreatedAt(LocalDateTime.now());
        clubMapper.insert(club);

        LinkedHashSet<Integer> tagIds = new LinkedHashSet<>();
        if (request.getTypeId() != null) {
            tagIds.add(request.getTypeId());
        }
        if (request.getTags() != null) {
            for (Integer tagId : request.getTags()) {
                if (tagId != null) {
                    tagIds.add(tagId);
                }
            }
        }
        for (Integer tagId : tagIds) {
            ClubTag clubTag = new ClubTag();
            clubTag.setClubId(club.getId());
            clubTag.setTagId(tagId);
            clubTag.setIsHidden(false);
            clubTagMapper.insert(clubTag);
        }

        /* JWT 约定：role = 社团 id + 1；有 relatedId 按 user.id 查，否则按登录名查 */
        User user =
                relatedId != null && relatedId > 0
                        ? userMapper.selectById(relatedId)
                        : userMapper.selectByUsername(jwtUtil.getUsernameFromToken(token));
        if (user != null) {
            Integer r = user.getRole();
            if (r == null || r == 0) {
                user.setRole(club.getId() + 1);
                userMapper.updateById(user);
            }
        }

        return club;
    }

    private static String buildCreateDescription(ClubRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            sb.append(request.getDescription().trim());
        }
        if (request.getActivityPlan() != null && !request.getActivityPlan().isBlank()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append("【活动计划】").append(request.getActivityPlan().trim());
        }
        if (request.getRequirements() != null && !request.getRequirements().isBlank()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append("【招新要求】").append(request.getRequirements().trim());
        }
        String s = sb.toString();
        return s.isEmpty() ? null : s;
    }

    private static String requireBearerToken() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        return auth.substring(7).trim();
    }

    @PutMapping("/{id}")
    public Club updateClub(@PathVariable Integer id, @RequestBody ClubRequest request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        Club club = clubMapper.selectById(id);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(id)) {
                throw new RuntimeException("您没有权限修改该社团");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限修改社团");
        }

        club.setName(request.getName());
        club.setDescription(request.getDescription());
        club.setQqGroup(request.getQqGroup());
        clubMapper.updateById(club);

        clubTagMapper.delete(new LambdaQueryWrapper<ClubTag>().eq(ClubTag::getClubId, id));

        if (request.getTags() != null) {
            for (Integer tagId : request.getTags()) {
                ClubTag clubTag = new ClubTag();
                clubTag.setClubId(club.getId());
                clubTag.setTagId(tagId);
                clubTag.setIsHidden(false);
                clubTagMapper.insert(clubTag);
            }
        }

        return club;
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteClub(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        Club club = clubMapper.selectById(id);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(id)) {
                throw new RuntimeException("您没有权限删除该社团");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限删除社团");
        }

        clubTagMapper.delete(new LambdaQueryWrapper<ClubTag>().eq(ClubTag::getClubId, id));
        clubMapper.deleteById(id);

        return Result.ok("社团删除成功");
    }

    @GetMapping("/{id:\\d+}/recommendations")
    public List<Map<String, Object>> getClubRecommendations(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(id)) {
                throw new RuntimeException("您没有权限查看该社团的推荐学生");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限查看推荐学生");
        }

        List<AiRecommendation> recommendations = aiRecommendationMapper.selectByClubId(id);
        List<Map<String, Object>> result = new ArrayList<>();

        for (AiRecommendation rec : recommendations) {
            User u = userMapper.selectById(rec.getUserId());
            if (u != null) {
                Map<String, Object> recMap = new HashMap<>();
                recMap.put("id", rec.getId());
                recMap.put("studentDbId", u.getId());
                recMap.put("studentId", u.getStudentId());
                String name = u.getRealName();
                recMap.put("studentName", name != null && !name.isBlank() ? name : u.getUsername());
                recMap.put("specialty", u.getSpecialty());
                recMap.put("hobby", u.getHobby());
                recMap.put("expectation", u.getExpectation());
                recMap.put("recommendationText", rec.getRecommendationText());
                recMap.put("similarity", rec.getSimilarity());
                recMap.put("status", rec.getStatus());
                recMap.put("createdAt", rec.getCreatedAt());
                result.add(recMap);
            }
        }

        return result;
    }

    @GetMapping("/{id:\\d+}/similar")
    public List<Map<String, Object>> getSimilarClubs(@PathVariable Integer id) {
        log.info("开始查询相似社团，clubId={}", id);
        Club currentClub = clubMapper.selectById(id);
        if (currentClub == null) {
            log.error("社团不存在，clubId={}", id);
            throw new RuntimeException("社团不存在");
        }
        log.info("社团存在，开始计算相似度，clubId={}, clubName={}", id, currentClub.getName());

        try {
            List<Map<String, Object>> result = clubInterMemberMatchService.rankSimilarClubsByMemberMatch(id, 4);
            log.info("相似社团计算完成，返回 {} 个结果", result.size());
            return result;
        } catch (Exception e) {
            log.error("计算相似社团失败，clubId={}, error={}", id, e.getMessage(), e);
            throw new RuntimeException("计算相似社团失败: " + e.getMessage());
        }
    }

    @PutMapping("/recommendations/{recId}/status")
    public Map<String, String> updateRecommendationStatus(
            @PathVariable Integer recId, @RequestBody Map<String, String> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        AiRecommendation rec = aiRecommendationMapper.selectById(recId);
        if (rec == null) {
            throw new RuntimeException("推荐记录不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(rec.getClubId())) {
                throw new RuntimeException("您没有权限修改此推荐状态");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限修改推荐状态");
        }

        String newStatus = request.get("status");
        if (newStatus != null) {
            rec.setStatus(newStatus);
            aiRecommendationMapper.updateById(rec);
        }

        Map<String, String> result = new HashMap<>();
        result.put("message", "状态更新成功");
        return result;
    }

    @PutMapping("/{id}/logo")
    public Result<String> updateClubLogo(
            @PathVariable Integer id, @RequestBody Map<String, String> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未授权访问");
        }
        String token = authHeader.substring(7);

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(id)) {
                throw new RuntimeException("您没有权限修改该社团Logo");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限修改社团Logo");
        }

        Club club = clubMapper.selectById(id);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        String logoUrl = request.get("logoUrl");
        if (logoUrl == null || logoUrl.isEmpty()) {
            logoUrl = request.get("logo");
        }
        if (logoUrl == null || logoUrl.isEmpty()) {
            throw new RuntimeException("Logo URL不能为空");
        }

        club.setLogoUrl(logoUrl);
        clubMapper.updateById(club);

        return Result.ok("Logo更新成功");
    }

    @GetMapping("/search")
    public Map<String, Object> searchClub(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();

        String k = keyword == null ? "" : keyword.trim();
        Club club = null;
        String matchType = null;
        boolean qqBoostEligible = false;

        if (k.startsWith("0") && k.length() > 1) {
            String idPart = k.substring(1).trim();
            if (!idPart.isEmpty()) {
                try {
                    int cid = Integer.parseInt(idPart);
                    if (cid > 0) {
                        club = clubMapper.selectById(cid);
                        if (club != null) {
                            matchType = "id";
                            qqBoostEligible = true;
                        }
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        if (club == null) {
            try {
                Integer clubId = Integer.parseInt(k);
                club = clubMapper.selectById(clubId);
                if (club != null) {
                    matchType = "id";
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (club == null) {
            List<Club> byName = clubMapper.selectByNameContaining(k);
            if (!byName.isEmpty()) {
                club = byName.get(0);
                matchType = "name";
            }
        }

        if (club == null) {
            club = findClubByQqKeyword(k);
            if (club != null) {
                matchType = "qq";
            }
        }

        if (club == null) {
            result.put("found", false);
            result.put("message", "未找到该社团");
            result.put("qqBoostEligible", false);
            return okEnvelope(result);
        }

        result.put("found", true);
        result.put("matchType", matchType != null ? matchType : "name");
        result.put("qqBoostEligible", qqBoostEligible);
        result.put("id", club.getId());
        result.put("name", club.getName());
        result.put("description", club.getDescription());
        result.put("qqGroup", club.getQqGroup());
        result.put("logoUrl", club.getLogoUrl());
        result.put("cover", club.getLogoUrl());
        result.put("logo", club.getLogoUrl());

        return okEnvelope(result);
    }

    private Club findClubByQqKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        String normKw = keyword.replaceAll("\\s+", "");
        if (normKw.isEmpty()) {
            return null;
        }
        for (Club c : clubMapper.selectList(null)) {
            if (c.getQqGroup() == null || c.getQqGroup().trim().isEmpty()) {
                continue;
            }
            String qn = c.getQqGroup().replaceAll("\\s+", "");
            if (qn.contains(normKw) || normKw.contains(qn)) {
                return c;
            }
        }
        return null;
    }

    @PutMapping("/{id}/qq-group")
    public Map<String, String> updateQqGroup(
            @PathVariable Integer id, @RequestBody Map<String, String> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未授权访问");
        }
        String token = authHeader.substring(7);

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(id)) {
                throw new RuntimeException("您没有权限修改该社团QQ群");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限修改社团QQ群");
        }

        Club club = clubMapper.selectById(id);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        String qqGroup = request.get("qqGroup");
        club.setQqGroup(qqGroup);
        clubMapper.updateById(club);

        Map<String, String> res = new HashMap<>();
        res.put("message", "QQ群更新成功");
        return res;
    }
}
