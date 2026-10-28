/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.common.entity.InviteCode;
import com.endtiem.common.mapper.InviteCodeMapper;
import com.endtiem.club.entity.ActivityGroup;
import com.endtiem.club.entity.ActivityGroupMember;
import com.endtiem.club.entity.ActivitySignup;
import com.endtiem.club.entity.ActivityWork;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubActivity;
import com.endtiem.club.mapper.ActivityGroupMapper;
import com.endtiem.club.mapper.ActivityGroupMemberMapper;
import com.endtiem.club.mapper.ActivitySignupMapper;
import com.endtiem.club.mapper.ActivityWorkMapper;
import com.endtiem.club.mapper.ClubActivityMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
@RequestMapping("/api/activities")
public class ClubActivityController {

    private final ClubActivityMapper clubActivityMapper;
    private final ClubMapper clubMapper;
    private final ActivityGroupMapper activityGroupMapper;
    private final ActivityGroupMemberMapper activityGroupMemberMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final ActivityWorkMapper activityWorkMapper;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;
    private final InviteCodeMapper inviteCodeMapper;

    public ClubActivityController(
            ClubActivityMapper clubActivityMapper,
            ClubMapper clubMapper,
            ActivityGroupMapper activityGroupMapper,
            ActivityGroupMemberMapper activityGroupMemberMapper,
            ActivitySignupMapper activitySignupMapper,
            ActivityWorkMapper activityWorkMapper,
            JwtUtil jwtUtil,
            JdbcTemplate jdbcTemplate,
            InviteCodeMapper inviteCodeMapper) {
        this.clubActivityMapper = clubActivityMapper;
        this.clubMapper = clubMapper;
        this.activityGroupMapper = activityGroupMapper;
        this.activityGroupMemberMapper = activityGroupMemberMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.activityWorkMapper = activityWorkMapper;
        this.jwtUtil = jwtUtil;
        this.jdbcTemplate = jdbcTemplate;
        this.inviteCodeMapper = inviteCodeMapper;
    }

    @GetMapping
    public Map<String, Object> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean sortByMatch) {
        int safeSize = Math.min(Math.max(size, 1), 50);

        if (sortByMatch) {
            Integer userId = getUserIdOrNull();
            if (userId != null) {
                Set<Integer> userTagIds = getUserTagIds(userId);
                if (!userTagIds.isEmpty()) {
                    List<ClubActivity> allActivities =
                            clubActivityMapper.selectByStatusOrderByCreatedAtDesc(1);
                    List<ActivityWithScore> scored =
                            allActivities.stream()
                                    .map(
                                            activity -> {
                                                Set<Integer> activityTagIds =
                                                        parseTagsToIds(activity.getTags());
                                                double similarity =
                                                        calculateJaccardSimilarity(
                                                                userTagIds, activityTagIds);
                                                return new ActivityWithScore(activity, similarity);
                                            })
                                    .filter(as -> as.score > 0)
                                    .sorted((a, b) -> Double.compare(b.score, a.score))
                                    .collect(Collectors.toList());

                    int total = scored.size();
                    int totalPages = safeSize > 0 ? (int) Math.ceil(total / (double) safeSize) : 0;
                    int from = Math.max(page, 0) * safeSize;
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (from < total) {
                        int to = Math.min(from + safeSize, total);
                        for (int i = from; i < to; i++) {
                            ActivityWithScore as = scored.get(i);
                            Map<String, Object> item = toSummaryMap(as.activity);
                            item.put("similarity", as.score);
                            result.add(item);
                        }
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("activities", result);
                    response.put("total", (long) total);
                    response.put("pages", totalPages);
                    response.put("currentPage", page);
                    return response;
                }
            }
        }

        Page<ClubActivity> activityPage =
                clubActivityMapper.pageByStatusOrderByCreatedAtDesc(
                        1, new Page<>(Math.max(page, 0) + 1L, safeSize));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ClubActivity activity : activityPage.getRecords()) {
            result.add(toSummaryMap(activity));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("activities", result);
        response.put("total", activityPage.getTotal());
        response.put("pages", activityPage.getPages());
        response.put("currentPage", page);
        return response;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getActivityById(@PathVariable Integer id) {
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        return toDetailMap(activity);
    }

    @GetMapping("/club/{clubId}")
    public List<Map<String, Object>> getActivitiesByClub(@PathVariable Integer clubId) {
        List<ClubActivity> activities =
                clubActivityMapper.selectByClubIdOrderByCreatedAtDesc(clubId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ClubActivity activity : activities) {
            result.add(toDetailMap(activity));
        }
        return result;
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingActivities() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role != 1) {
            throw new RuntimeException("只有系统管理员可以查看待审批活动");
        }

        List<ClubActivity> activities = clubActivityMapper.selectByStatusOrderByCreatedAtDesc(0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ClubActivity activity : activities) {
            result.add(toSummaryMap(activity));
        }
        return result;
    }

    @PostMapping
    public ClubActivity createActivity(@RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role < 2) {
            throw new RuntimeException("只有社团管理员可以申请活动");
        }

        Integer clubId = toInteger(request.get("clubId"));
        Integer managedClubId = role - 1;
        if (!managedClubId.equals(clubId)) {
            throw new RuntimeException("您只能为自己的社团申请活动");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todayCount = clubActivityMapper.countTodayByClubId(clubId, startOfDay, endOfDay);
        if (todayCount > 0) {
            throw new RuntimeException("该社团今天已经发布过活动，每天只能发布一个活动");
        }

        ClubActivity activity = new ClubActivity();
        activity.setClubId(clubId);
        activity.setTitle((String) request.get("title"));
        activity.setContent((String) request.get("content"));
        activity.setImageUrl((String) request.get("imageUrl"));
        activity.setActivityTime(toLocalDateTime(request.get("activityTime")));
        activity.setLocation((String) request.get("location"));
        activity.setTags((String) request.get("tags"));
        activity.setStatus(0);
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);

        clubActivityMapper.insert(activity);
        return activity;
    }

    @PutMapping("/{id}")
    public ClubActivity updateActivity(
            @PathVariable Integer id, @RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role < 2) {
            throw new RuntimeException("只有社团管理员可以编辑活动");
        }

        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        Integer managedClubId = role - 1;
        if (!managedClubId.equals(activity.getClubId())) {
            throw new RuntimeException("您只能编辑自己社团的活动");
        }

        if (activity.getStatus() != 0 && activity.getStatus() != 2) {
            throw new RuntimeException("只能编辑待审批或已拒绝的活动");
        }

        if (request.get("title") != null) {
            activity.setTitle((String) request.get("title"));
        }
        if (request.get("content") != null) {
            activity.setContent((String) request.get("content"));
        }
        if (request.get("imageUrl") != null) {
            activity.setImageUrl((String) request.get("imageUrl"));
        }
        if (request.get("activityTime") != null) {
            activity.setActivityTime(toLocalDateTime(request.get("activityTime")));
        }
        if (request.get("location") != null) {
            activity.setLocation((String) request.get("location"));
        }
        if (request.get("tags") != null) {
            activity.setTags((String) request.get("tags"));
        }
        activity.setStatus(0);
        activity.setRejectReason(null);
        activity.setUpdatedAt(LocalDateTime.now());

        clubActivityMapper.updateById(activity);
        return activity;
    }

    @PutMapping("/{id}/approve")
    public ClubActivity approveActivity(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role != 1) {
            throw new RuntimeException("只有系统管理员可以审批活动");
        }

        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        if (activity.getStatus() != 0) {
            throw new RuntimeException("该活动已审批");
        }

        activity.setStatus(1);
        activity.setReviewedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        clubActivityMapper.updateById(activity);
        return activity;
    }

    @PutMapping("/{id}/reject")
    public ClubActivity rejectActivity(
            @PathVariable Integer id, @RequestBody(required = false) Map<String, Object> body) {
        String rejectReason = "";
        if (body != null) {
            Object r = body.get("reason");
            if (r == null) {
                r = body.get("rejectReason");
            }
            if (r != null) {
                rejectReason = String.valueOf(r);
            }
        }
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role != 1) {
            throw new RuntimeException("只有系统管理员可以审批活动");
        }

        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        if (activity.getStatus() != 0) {
            throw new RuntimeException("该活动已审批");
        }

        activity.setStatus(2);
        activity.setRejectReason(rejectReason.isEmpty() ? "已驳回" : rejectReason);
        activity.setReviewedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        clubActivityMapper.updateById(activity);
        return activity;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteActivity(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        if (role == 1) {
        } else if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(activity.getClubId())) {
                throw new RuntimeException("您只能删除自己社团的活动");
            }
        } else {
            throw new RuntimeException("没有权限删除活动");
        }

        clubActivityMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "活动已删除");
        return result;
    }

    @PostMapping("/groups")
    public Map<String, Object> createGroup(@RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role < 2) {
            throw new RuntimeException("只有社团管理员可以创建活动组合");
        }

        Integer clubId = toInteger(request.get("clubId"));
        Integer managedClubId = role - 1;
        if (!managedClubId.equals(clubId)) {
            throw new RuntimeException("您只能为自己的社团创建活动组合");
        }

        String name = (String) request.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("组合名称不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        ActivityGroup group = new ActivityGroup();
        group.setClubId(clubId);
        group.setName(name.trim());
        group.setDescription((String) request.get("description"));
        group.setMinSize(
                toInteger(request.get("minSize")) != null ? toInteger(request.get("minSize")) : 1);
        group.setMaxSize(
                toInteger(request.get("maxSize")) != null ? toInteger(request.get("maxSize")) : 10);
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        activityGroupMapper.insert(group);

        Map<String, Object> result = new HashMap<>();
        result.put("id", group.getId());
        result.put("name", group.getName());
        result.put("clubId", group.getClubId());
        result.put("minSize", group.getMinSize());
        result.put("maxSize", group.getMaxSize());
        result.put("createdAt", group.getCreatedAt());
        return result;
    }

    @GetMapping("/groups/club/{clubId}")
    public List<Map<String, Object>> getGroupsByClub(@PathVariable Integer clubId) {
        List<ActivityGroup> groups = activityGroupMapper.selectByClubId(clubId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivityGroup group : groups) {
            result.add(toGroupRow(group));
        }
        return result;
    }

    @GetMapping("/groups/{id}")
    public Map<String, Object> getGroupById(@PathVariable Integer id) {
        ActivityGroup group = activityGroupMapper.selectById(id);
        if (group == null) {
            throw new RuntimeException("活动组合不存在");
        }
        Map<String, Object> row = toGroupRow(group);

        List<ActivityGroupMember> members =
                activityGroupMemberMapper.selectByGroupId(id.longValue());
        List<Map<String, Object>> memberList = new ArrayList<>();
        for (ActivityGroupMember member : members) {
            Map<String, Object> memberRow = new HashMap<>();
            memberRow.put("id", member.getId());
            memberRow.put("userId", member.getUserId());
            memberRow.put("joinedAt", member.getJoinedAt());
            memberList.add(memberRow);
        }
        row.put("members", memberList);
        return row;
    }

    @PutMapping("/groups/{id}")
    public Map<String, Object> updateGroup(
            @PathVariable Integer id, @RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role < 2) {
            throw new RuntimeException("只有社团管理员可以编辑活动组合");
        }

        ActivityGroup group = activityGroupMapper.selectById(id);
        if (group == null) {
            throw new RuntimeException("活动组合不存在");
        }

        Integer managedClubId = role - 1;
        if (!managedClubId.equals(group.getClubId())) {
            throw new RuntimeException("您只能编辑自己社团的活动组合");
        }

        if (request.get("name") != null) {
            String name = request.get("name").toString().trim();
            if (name.isEmpty()) {
                throw new RuntimeException("组合名称不能为空");
            }
            group.setName(name);
        }
        if (request.get("description") != null) {
            group.setDescription(request.get("description").toString());
        }
        if (request.get("minSize") != null) {
            group.setMinSize(toInteger(request.get("minSize")));
        }
        if (request.get("maxSize") != null) {
            group.setMaxSize(toInteger(request.get("maxSize")));
        }
        group.setUpdatedAt(LocalDateTime.now());
        activityGroupMapper.updateById(group);

        return toGroupRow(group);
    }

    @DeleteMapping("/groups/{id}")
    public Map<String, Object> deleteGroup(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        ActivityGroup group = activityGroupMapper.selectById(id);
        if (group == null) {
            throw new RuntimeException("活动组合不存在");
        }

        if (role == 1) {
        } else if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(group.getClubId())) {
                throw new RuntimeException("您只能删除自己社团的活动组合");
            }
        } else {
            throw new RuntimeException("没有权限删除活动组合");
        }

        activityGroupMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "活动组合已删除");
        return result;
    }

    @PostMapping("/groups/{id}/join")
    public Map<String, Object> joinGroup(
            @PathVariable Integer id, @RequestBody(required = false) Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();

        ActivityGroup group = activityGroupMapper.selectById(id);
        if (group == null) {
            throw new RuntimeException("活动组合不存在");
        }

        ActivityGroupMember existing =
                activityGroupMemberMapper.selectByGroupIdAndUserId(
                        id.longValue(), userId.longValue());
        if (existing != null && existing.getStatus() == 1) {
            throw new RuntimeException("您已经加入此组合");
        }

        long currentCount = activityGroupMemberMapper.countActiveMembers(id.longValue());
        if (currentCount >= group.getMaxSize()) {
            throw new RuntimeException("此组合人数已满");
        }

        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setStatus(1);
            existing.setJoinedAt(now);
            activityGroupMemberMapper.updateById(existing);
        } else {
            ActivityGroupMember member = new ActivityGroupMember();
            member.setGroupId(id.longValue());
            member.setUserId(userId.longValue());
            member.setActivityId(
                    request != null && request.get("activityId") != null
                            ? toInteger(request.get("activityId")).longValue()
                            : null);
            member.setStatus(1);
            member.setJoinedAt(now);
            member.setCreatedAt(now);
            activityGroupMemberMapper.insert(member);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("groupId", id);
        result.put("memberCount", activityGroupMemberMapper.countActiveMembers(id.longValue()));
        return result;
    }

    @DeleteMapping("/groups/{id}/join")
    public Map<String, Object> leaveGroup(@PathVariable Integer id) {
        Integer userId = requireUserIdFromToken();

        ActivityGroupMember member =
                activityGroupMemberMapper.selectByGroupIdAndUserId(
                        id.longValue(), userId.longValue());
        if (member == null || member.getStatus() != 1) {
            throw new RuntimeException("您未加入此组合");
        }

        member.setStatus(2);
        activityGroupMemberMapper.updateById(member);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("groupId", id);
        return result;
    }

    @GetMapping("/groups/my")
    public List<Map<String, Object>> getMyGroups() {
        Integer userId = requireUserIdFromToken();

        List<ActivityGroupMember> memberships =
                activityGroupMemberMapper.selectByUserId(userId.longValue());
        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivityGroupMember membership : memberships) {
            ActivityGroup group =
                    activityGroupMapper.selectById(membership.getGroupId().intValue());
            if (group != null) {
                Map<String, Object> row = toGroupRow(group);
                row.put("joinedAt", membership.getJoinedAt());
                row.put(
                        "memberCount",
                        activityGroupMemberMapper.countActiveMembers(group.getId().longValue()));
                result.add(row);
            }
        }
        return result;
    }

    @GetMapping("/{id}/signups")
    public List<Map<String, Object>> getActivitySignups(@PathVariable Integer id) {
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        List<ActivitySignup> signups = activitySignupMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getActivityId, id)
                        .eq(ActivitySignup::getStatus, 1)
                        .orderByDesc(ActivitySignup::getSignupTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivitySignup signup : signups) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", signup.getId());
            item.put("userId", signup.getUserId());
            item.put("signupTime", signup.getSignupTime());
            item.put("createdAt", signup.getCreatedAt());

            Map<String, Object> userInfo = getUserInfo(signup.getUserId());
            item.put("username", userInfo.get("username"));
            item.put("nickname", userInfo.get("nickname"));
            item.put("realName", userInfo.get("realName"));
            item.put("avatar", userInfo.get("avatar"));

            result.add(item);
        }
        return result;
    }

    @DeleteMapping("/{id}/signup")
    public Map<String, Object> cancelSignup(@PathVariable Integer id) {
        Integer userId = requireUserIdFromToken();

        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        ActivitySignup signup = activitySignupMapper.selectByActivityIdAndUserId(id, userId);
        if (signup == null || signup.getStatus() != 1) {
            throw new RuntimeException("您未报名此活动");
        }

        activitySignupMapper.deleteByActivityIdAndUserId(id, userId);

        activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
        clubActivityMapper.updateById(activity);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "取消报名成功");
        return result;
    }

    @GetMapping("/my")
    public List<Map<String, Object>> getMyActivities() {
        Integer userId = requireUserIdFromToken();

        List<ActivitySignup> signups = activitySignupMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getUserId, userId)
                        .eq(ActivitySignup::getStatus, 1)
                        .orderByDesc(ActivitySignup::getSignupTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivitySignup signup : signups) {
            ClubActivity activity = clubActivityMapper.selectById(signup.getActivityId());
            if (activity != null && activity.getStatus() == 1) {
                Map<String, Object> item = toSummaryMap(activity);
                item.put("signupTime", signup.getSignupTime());
                item.put("signedUp", true);
                result.add(item);
            }
        }
        return result;
    }

    private Map<String, Object> getUserInfo(Integer userId) {
        Map<String, Object> info = new HashMap<>();
        info.put("username", "");
        info.put("nickname", "");
        info.put("realName", "");
        info.put("avatar", "");

        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                    "SELECT username, nickname, real_name, avatar FROM user WHERE id = ?", userId);
            if (!users.isEmpty()) {
                Map<String, Object> user = users.get(0);
                info.put("username", user.get("username") != null ? user.get("username") : "");
                info.put("nickname", user.get("nickname") != null ? user.get("nickname") : "");
                info.put("realName", user.get("real_name") != null ? user.get("real_name") : "");
                info.put("avatar", user.get("avatar") != null ? user.get("avatar") : "");
            }
        } catch (Exception e) {
        }

        return info;
    }

    private Map<String, Object> toGroupRow(ActivityGroup group) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", group.getId());
        row.put("clubId", group.getClubId());
        row.put("clubName", resolveClubName(group.getClubId()));
        row.put("name", group.getName());
        row.put("description", group.getDescription());
        row.put("minSize", group.getMinSize());
        row.put("maxSize", group.getMaxSize());
        row.put(
                "memberCount",
                activityGroupMemberMapper.countActiveMembers(group.getId().longValue()));
        row.put("createdAt", group.getCreatedAt());
        row.put("updatedAt", group.getUpdatedAt());
        return row;
    }

    private Map<String, Object> toSummaryMap(ClubActivity activity) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", activity.getId());
        item.put("clubId", activity.getClubId());
        item.put("clubName", resolveClubName(activity.getClubId()));
        item.put("title", activity.getTitle());
        item.put("content", activity.getContent());
        item.put("imageUrl", activity.getImageUrl());
        item.put("activityTime", activity.getActivityTime());
        item.put("location", activity.getLocation());
        item.put("tags", activity.getTags());
        item.put("status", activity.getStatus());
        item.put("createdAt", activity.getCreatedAt());
        return item;
    }

    private Map<String, Object> toDetailMap(ClubActivity activity) {
        Map<String, Object> item = toSummaryMap(activity);
        item.put("rejectReason", activity.getRejectReason());
        item.put("reviewedAt", activity.getReviewedAt());
        return item;
    }

    private String resolveClubName(Integer clubId) {
        if (clubId == null) {
            return null;
        }
        Club club = clubMapper.selectById(clubId);
        return club != null ? club.getName() : null;
    }

    private Integer requireUserIdFromToken() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
        Integer userId = jwtUtil.getAccountUserIdFromToken(token);
        if (userId == null) {
            Integer relatedId = jwtUtil.getRelatedIdFromToken(token);
            if (relatedId != null) {
                userId = relatedId;
            }
        }
        if (userId == null) {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null || username.isBlank()) {
                throw new RuntimeException("请先登录");
            }
        }
        return userId;
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

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        long ms;
        if (value instanceof Number) {
            ms = ((Number) value).longValue();
        } else {
            ms = Long.parseLong(value.toString());
        }
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDateTime();
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
            Integer userId = jwtUtil.getRelatedIdFromToken(token);
            return userId;
        } catch (Exception e) {
            return null;
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

    private static final class ActivityWithScore {
        final ClubActivity activity;
        final double score;

        ActivityWithScore(ClubActivity activity, double score) {
            this.activity = activity;
            this.score = score;
        }
    }

    @PostMapping("/{id}/signup-pair")
    public Map<String, Object> signupPair(
            @PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        Integer maxParticipants = activity.getMaxParticipants();
        if (maxParticipants != null && activity.getCurrentParticipants() >= maxParticipants) {
            throw new RuntimeException("活动报名人数已满");
        }

        ActivitySignup existingSignup = activitySignupMapper.selectByActivityIdAndUserId(id, userId);
        if (existingSignup != null && existingSignup.getStatus() == 1) {
            throw new RuntimeException("您已经报名此活动");
        }

        String partnerAccount = request.get("partnerAccount") != null ? request.get("partnerAccount").toString().trim() : null;
        String clubKey1 = request.get("clubKey1") != null ? request.get("clubKey1").toString().trim() : null;
        String clubKey2 = request.get("clubKey2") != null ? request.get("clubKey2").toString().trim() : null;

        List<Integer> clubIds = new ArrayList<>();

        if (clubKey1 != null && !clubKey1.isEmpty()) {
            InviteCode inviteCode1 = inviteCodeMapper.selectByCode(clubKey1);
            if (inviteCode1 == null || inviteCode1.getStatus() != 1) {
                throw new RuntimeException("社团邀请码1无效");
            }
            if (inviteCode1.getExpiresAt() != null && inviteCode1.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("社团邀请码1已过期");
            }
            if (inviteCode1.getClubId() != null) {
                clubIds.add(inviteCode1.getClubId());
                inviteCodeMapper.incrementUsedCount(clubKey1);
            }
        }

        if (clubKey2 != null && !clubKey2.isEmpty()) {
            InviteCode inviteCode2 = inviteCodeMapper.selectByCode(clubKey2);
            if (inviteCode2 == null || inviteCode2.getStatus() != 1) {
                throw new RuntimeException("社团邀请码2无效");
            }
            if (inviteCode2.getExpiresAt() != null && inviteCode2.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("社团邀请码2已过期");
            }
            if (inviteCode2.getClubId() != null) {
                if (!clubIds.contains(inviteCode2.getClubId())) {
                    clubIds.add(inviteCode2.getClubId());
                }
                inviteCodeMapper.incrementUsedCount(clubKey2);
            }
        }

        int participantCount = 1;
        if (partnerAccount != null && !partnerAccount.isEmpty()) {
            participantCount = 2;
        }

        if (maxParticipants != null && activity.getCurrentParticipants() + participantCount > maxParticipants) {
            throw new RuntimeException("剩余名额不足，无法报名");
        }

        LocalDateTime now = LocalDateTime.now();
        if (existingSignup != null) {
            existingSignup.setStatus(1);
            existingSignup.setSignupTime(now);
            activitySignupMapper.updateById(existingSignup);
        } else {
            ActivitySignup signup = new ActivitySignup();
            signup.setActivityId(id);
            signup.setUserId(userId);
            signup.setStatus(1);
            signup.setSignupTime(now);
            signup.setCreatedAt(now);
            activitySignupMapper.insert(signup);
        }

        activity.setCurrentParticipants(activity.getCurrentParticipants() + participantCount);
        clubActivityMapper.updateById(activity);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "报名成功");
        result.put("activityId", id);
        result.put("participantCount", participantCount);
        result.put("clubIds", clubIds);
        return result;
    }

    @GetMapping("/{id}/works")
    public List<Map<String, Object>> getActivityWorks(@PathVariable Integer id) {
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        List<ActivityWork> works = activityWorkMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityWork>()
                        .eq(ActivityWork::getActivityId, id)
                        .eq(ActivityWork::getStatus, 1)
                        .orderByDesc(ActivityWork::getCreatedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivityWork work : works) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", work.getId());
            item.put("activityId", work.getActivityId());
            item.put("userId", work.getUserId());
            item.put("title", work.getTitle());
            item.put("content", work.getContent());
            item.put("likesCount", work.getLikesCount());
            item.put("createdAt", work.getCreatedAt());
            item.put("updatedAt", work.getUpdatedAt());

            Map<String, Object> userInfo = getUserInfo(work.getUserId());
            item.put("username", userInfo.get("username"));
            item.put("nickname", userInfo.get("nickname"));
            item.put("realName", userInfo.get("realName"));
            item.put("avatar", userInfo.get("avatar"));

            result.add(item);
        }
        return result;
    }

    @PostMapping("/{id}/works")
    public Map<String, Object> createWork(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();

        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        ActivitySignup signup = activitySignupMapper.selectByActivityIdAndUserId(id, userId);
        if (signup == null || signup.getStatus() != 1) {
            throw new RuntimeException("您必须先报名此活动才能提交作品");
        }

        ActivityWork existingWork = activityWorkMapper.selectByActivityIdAndUserId(id, userId);
        if (existingWork != null) {
            throw new RuntimeException("您已经提交过作品了，可以使用更新功能修改您的作品");
        }

        String title = request.get("title") != null ? request.get("title").toString().trim() : null;
        String content = request.get("content") != null ? request.get("content").toString().trim() : null;

        if (title == null || title.isEmpty()) {
            throw new RuntimeException("作品标题不能为空");
        }
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("作品内容不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        ActivityWork work = new ActivityWork();
        work.setActivityId(id);
        work.setUserId(userId);
        work.setTitle(title);
        work.setContent(content);
        work.setLikesCount(0);
        work.setStatus(1);
        work.setCreatedAt(now);
        work.setUpdatedAt(now);
        activityWorkMapper.insert(work);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "作品提交成功");
        result.put("workId", work.getId());
        return result;
    }

    @PutMapping("/{id}/works")
    public Map<String, Object> updateWork(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();

        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        ActivityWork existingWork = activityWorkMapper.selectByActivityIdAndUserId(id, userId);
        if (existingWork == null) {
            throw new RuntimeException("您还没有提交作品");
        }

        if (request.get("title") != null) {
            String title = request.get("title").toString().trim();
            if (!title.isEmpty()) {
                existingWork.setTitle(title);
            }
        }
        if (request.get("content") != null) {
            String content = request.get("content").toString().trim();
            if (!content.isEmpty()) {
                existingWork.setContent(content);
            }
        }
        existingWork.setUpdatedAt(LocalDateTime.now());
        activityWorkMapper.updateById(existingWork);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "作品更新成功");
        return result;
    }

    @GetMapping("/{id}/works/my")
    public Map<String, Object> getMyWork(@PathVariable Integer id) {
        Integer userId = requireUserIdFromToken();

        ActivityWork work = activityWorkMapper.selectByActivityIdAndUserId(id, userId);
        if (work == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasWork", false);
            result.put("work", null);
            return result;
        }

        Map<String, Object> item = new HashMap<>();
        item.put("id", work.getId());
        item.put("activityId", work.getActivityId());
        item.put("userId", work.getUserId());
        item.put("title", work.getTitle());
        item.put("content", work.getContent());
        item.put("likesCount", work.getLikesCount());
        item.put("createdAt", work.getCreatedAt());
        item.put("updatedAt", work.getUpdatedAt());

        Map<String, Object> result = new HashMap<>();
        result.put("hasWork", true);
        result.put("work", item);
        return result;
    }
}
