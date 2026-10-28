/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.Club;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.entity.Invitation;
import com.endtiem.platform.mapper.InvitationMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationMapper invitationMapper;
    private final ClubMapper clubMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public InvitationController(
            InvitationMapper invitationMapper,
            ClubMapper clubMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil) {
        this.invitationMapper = invitationMapper;
        this.clubMapper = clubMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public Map<String, Object> createInvitation(@RequestBody Map<String, Object> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role < 2 && role != 1) {
            throw new RuntimeException("您没有权限发送邀请");
        }

        Integer clubId = toInteger(request.get("clubId"));
        Integer userId = toInteger(request.get("userId"));
        Integer aiRecommendationId = toInteger(request.get("aiRecommendationId"));
        String message = (String) request.get("message");

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(clubId)) {
                throw new RuntimeException("您只能邀请用户加入您管理的社团");
            }
        }

        Invitation existing = invitationMapper.selectByClubIdAndUserId(clubId, userId);
        if (existing != null) {
            throw new RuntimeException("该用户已被邀请");
        }

        Invitation invitation = new Invitation();
        invitation.setClubId(clubId);
        invitation.setUserId(userId);
        invitation.setAiRecommendationId(aiRecommendationId);
        invitation.setMessage(message);
        invitation.setStatus("pending");
        invitation.setCreatedAt(LocalDateTime.now());

        invitationMapper.insert(invitation);

        Map<String, Object> result = new HashMap<>();
        result.put("id", invitation.getId());
        result.put("message", "邀请发送成功");
        return result;
    }

    @GetMapping("/my")
    public List<Map<String, Object>> getMyInvitations() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        List<Invitation> invitations = invitationMapper.selectByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Invitation inv : invitations) {
            Club club = clubMapper.selectById(inv.getClubId());
            if (club != null) {
                Map<String, Object> invMap = new HashMap<>();
                invMap.put("id", inv.getId());
                invMap.put("clubId", club.getId());
                invMap.put("clubName", club.getName());
                invMap.put("clubDescription", club.getDescription());
                invMap.put("message", inv.getMessage());
                invMap.put("status", inv.getStatus());
                invMap.put("createdAt", inv.getCreatedAt());
                result.add(invMap);
            }
        }

        return result;
    }

    @GetMapping("/club/{clubId}")
    public List<Map<String, Object>> getClubInvitations(@PathVariable Integer clubId) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(clubId)) {
                throw new RuntimeException("您没有权限查看该社团的邀请");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限查看邀请");
        }

        List<Invitation> invitations = invitationMapper.selectByClubId(clubId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Invitation inv : invitations) {
            User user = userMapper.selectById(inv.getUserId());
            if (user != null) {
                Map<String, Object> invMap = new HashMap<>();
                invMap.put("id", inv.getId());
                invMap.put("userId", user.getId());
                invMap.put("userName", user.getRealName());
                invMap.put("message", inv.getMessage());
                invMap.put("status", inv.getStatus());
                invMap.put("createdAt", inv.getCreatedAt());
                result.add(invMap);
            }
        }

        return result;
    }

    @PutMapping("/{id}/accept")
    public Map<String, String> acceptInvitation(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        Invitation invitation = invitationMapper.selectById(id);
        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        if (!invitation.getUserId().equals(userId)) {
            throw new RuntimeException("这不是发给您的邀请");
        }

        invitation.setStatus("accepted");
        invitationMapper.updateById(invitation);

        Map<String, String> result = new HashMap<>();
        result.put("message", "已接受邀请");
        result.put("clubId", String.valueOf(invitation.getClubId()));
        return result;
    }

    @PutMapping("/{id}/reject")
    public Map<String, String> rejectInvitation(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        Invitation invitation = invitationMapper.selectById(id);
        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        if (!invitation.getUserId().equals(userId)) {
            throw new RuntimeException("这不是发给您的邀请");
        }

        invitation.setStatus("rejected");
        invitationMapper.updateById(invitation);

        Map<String, String> result = new HashMap<>();
        result.put("message", "已拒绝邀请");
        return result;
    }

    @PutMapping("/{id}/complete")
    public Map<String, String> completeInvitation(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        Invitation invitation = invitationMapper.selectById(id);
        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        if (!invitation.getUserId().equals(userId)) {
            throw new RuntimeException("这不是发给您的邀请");
        }

        invitation.setStatus("completed");
        invitationMapper.updateById(invitation);

        Map<String, String> result = new HashMap<>();
        result.put("message", "报名已完成");
        return result;
    }

    @GetMapping("/pending-count")
    public Map<String, Integer> getPendingCount() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        List<Invitation> pendingInvitations =
                invitationMapper.selectByUserIdAndStatus(userId, "pending");

        Map<String, Integer> result = new HashMap<>();
        result.put("count", pendingInvitations.size());
        return result;
    }

    @GetMapping("/verify/{code}")
    public Map<String, Object> verifyInvitationCode(@PathVariable String code) {
        Invitation invitation = invitationMapper.selectByCode(code);
        if (invitation == null) {
            throw new RuntimeException("邀请码无效");
        }

        if (!"pending".equals(invitation.getStatus()) && !"unused".equals(invitation.getStatus())) {
            throw new RuntimeException("邀请码已被使用或已失效");
        }

        Club club = clubMapper.selectById(invitation.getClubId());
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("clubId", club.getId());
        result.put("clubName", club.getName());
        result.put("clubDescription", club.getDescription());
        result.put("clubAvatar", club.getLogoUrl());

        return result;
    }

    @PostMapping("/use/{code}")
    public Map<String, Object> useInvitationCode(@PathVariable String code) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        Invitation invitation = invitationMapper.selectByCode(code);
        if (invitation == null) {
            throw new RuntimeException("邀请码无效");
        }

        if (!"pending".equals(invitation.getStatus()) && !"unused".equals(invitation.getStatus())) {
            throw new RuntimeException("邀请码已被使用或已失效");
        }

        invitation.setStatus("accepted");
        invitation.setUserId(userId);
        invitation.setUsedAt(LocalDateTime.now());
        invitationMapper.updateById(invitation);

        Club club = clubMapper.selectById(invitation.getClubId());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成功加入社团");
        result.put("clubId", invitation.getClubId());
        result.put("clubName", club != null ? club.getName() : null);

        return result;
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
