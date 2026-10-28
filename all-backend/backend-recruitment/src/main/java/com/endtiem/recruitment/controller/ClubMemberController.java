/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.entity.ClubMember;
import com.endtiem.recruitment.mapper.ClubMemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/club-members")
public class ClubMemberController {

    private final ClubMemberMapper clubMemberMapper;
    private final ClubMapper clubMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public ClubMemberController(
            ClubMemberMapper clubMemberMapper,
            ClubMapper clubMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil) {
        this.clubMemberMapper = clubMemberMapper;
        this.clubMapper = clubMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/club/{clubId}")
    public List<Map<String, Object>> getMembersByClub(@PathVariable Long clubId) {
        List<ClubMember> members = clubMemberMapper.selectByClubId(clubId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClubMember member : members) {
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("id", member.getId());
            memberInfo.put("clubId", member.getClubId());
            memberInfo.put("userId", member.getUserId());
            memberInfo.put("role", member.getRole());
            memberInfo.put("joinTime", member.getJoinTime());
            memberInfo.put("status", member.getStatus());

            User user = userMapper.selectById(member.getUserId().intValue());
            if (user != null) {
                memberInfo.put("studentId", user.getStudentId());
                memberInfo.put("name", user.getRealName());
                memberInfo.put("nickname", user.getNickname());
                memberInfo.put("avatar", user.getAvatar());
                memberInfo.put("college", user.getCollege());
                memberInfo.put("major", user.getMajor());
            }

            result.add(memberInfo);
        }

        return result;
    }

    @PostMapping("/join")
    public Map<String, Object> joinClub(@RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);

        Long clubId = toLong(request.get("clubId"));
        Long userId = relatedId.longValue();

        if (clubMemberMapper.existsByClubIdAndUserId(clubId, userId)) {
            throw new RuntimeException("您已经是该社团成员");
        }

        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(userId);
        member.setRole(1);
        member.setStatus(1);
        member.setJoinTime(LocalDateTime.now());
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        clubMemberMapper.insert(member);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "加入社团成功");
        result.put("memberId", member.getId());

        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> removeMember(@PathVariable Long id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        ClubMember member = clubMemberMapper.selectById(id);
        if (member == null) {
            throw new RuntimeException("成员不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(member.getClubId().intValue())) {
                throw new RuntimeException("您没有权限移除该成员");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限移除成员");
        }

        member.setStatus(3);
        member.setUpdatedAt(LocalDateTime.now());
        clubMemberMapper.updateById(member);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成员已移除");

        return result;
    }

    @DeleteMapping("/club/{clubId}/{userId}")
    public Map<String, Object> removeMemberByClubAndUser(
            @PathVariable Long clubId, @PathVariable Long userId) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(clubId.intValue())) {
                throw new RuntimeException("您没有权限移除该成员");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限移除成员");
        }

        List<ClubMember> members = clubMemberMapper.selectByClubId(clubId);
        for (ClubMember member : members) {
            if (member.getUserId().equals(userId)) {
                member.setStatus(3);
                member.setUpdatedAt(LocalDateTime.now());
                clubMemberMapper.updateById(member);
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成员已移除");

        return result;
    }

    @PutMapping("/{id}/role")
    public Map<String, Object> updateMemberRole(
            @PathVariable Long id, @RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        ClubMember member = clubMemberMapper.selectById(id);
        if (member == null) {
            throw new RuntimeException("成员不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(member.getClubId().intValue())) {
                throw new RuntimeException("您没有权限更新该成员角色");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限更新成员角色");
        }

        Integer newRole = toInteger(request.get("role"));
        if (newRole != null) {
            member.setRole(newRole);
            member.setUpdatedAt(LocalDateTime.now());
            clubMemberMapper.updateById(member);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "角色更新成功");

        return result;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Long) return (Long) o;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }

    private Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(o.toString());
    }
}
