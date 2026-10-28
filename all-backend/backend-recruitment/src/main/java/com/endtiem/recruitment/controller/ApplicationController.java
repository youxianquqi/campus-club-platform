/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.Club;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.dto.ApplicationRequest;
import com.endtiem.recruitment.entity.ClubMember;
import com.endtiem.recruitment.entity.MembershipApplication;
import com.endtiem.recruitment.mapper.ClubMemberMapper;
import com.endtiem.recruitment.mapper.MembershipApplicationMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final MembershipApplicationMapper applicationMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final JwtUtil jwtUtil;

    public ApplicationController(
            MembershipApplicationMapper applicationMapper,
            ClubMemberMapper clubMemberMapper,
            UserMapper userMapper,
            ClubMapper clubMapper,
            JwtUtil jwtUtil) {
        this.applicationMapper = applicationMapper;
        this.clubMemberMapper = clubMemberMapper;
        this.userMapper = userMapper;
        this.clubMapper = clubMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public MembershipApplication createApplication(@RequestBody ApplicationRequest request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        MembershipApplication existingApplication =
                applicationMapper.selectByUserIdAndClubId(userId, request.getClubId()).orElse(null);

        if (existingApplication != null) {
            String statusMsg;
            switch (existingApplication.getStatus()) {
                case 0:
                    statusMsg = "您已报名该社团，申请正在审核中";
                    break;
                case 1:
                    statusMsg = "您已通过该社团的审核，无需重复报名";
                    break;
                case 2:
                    statusMsg = "您之前的申请已被拒绝，原因：" + existingApplication.getRejectReason();
                    break;
                default:
                    statusMsg = "您已报名过该社团";
            }
            throw new RuntimeException(statusMsg);
        }

        MembershipApplication application = new MembershipApplication();
        application.setUserId(userId);
        application.setClubId(request.getClubId());
        application.setSelfIntro(request.getIntro());
        application.setPhone(request.getPhone());
        application.setStatus(0);

        applicationMapper.insert(application);
        return application;
    }

    @GetMapping("/my")
    public List<Map<String, Object>> getMyApplications() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        List<MembershipApplication> applications = applicationMapper.selectByUserId(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (MembershipApplication app : applications) {
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("id", app.getId());
            appInfo.put("clubId", app.getClubId());
            Club club = clubMapper.selectById(app.getClubId());
            appInfo.put("clubName", club != null ? club.getName() : null);
            appInfo.put("selfIntro", app.getSelfIntro());
            appInfo.put("phone", app.getPhone());
            appInfo.put("status", app.getStatus());
            appInfo.put("rejectReason", app.getRejectReason());
            appInfo.put("submittedAt", app.getSubmittedAt());
            appInfo.put("reviewedAt", app.getReviewedAt());
            result.add(appInfo);
        }
        return result;
    }

    @GetMapping("/club/{clubId}")
    public List<Map<String, Object>> getClubApplications(@PathVariable Integer clubId) {
        List<MembershipApplication> applications = applicationMapper.selectByClubId(clubId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (MembershipApplication app : applications) {
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("id", app.getId());
            appInfo.put("userId", app.getUserId());
            User u = userMapper.selectById(app.getUserId());
            if (u != null) {
                appInfo.put("studentName", u.getRealName());
                appInfo.put("studentStudentId", u.getStudentId());
                appInfo.put("studentAvatar", u.getAvatar());
            }
            appInfo.put("selfIntro", app.getSelfIntro());
            appInfo.put("phone", app.getPhone());
            appInfo.put("status", app.getStatus());
            appInfo.put("rejectReason", app.getRejectReason());
            appInfo.put("submittedAt", app.getSubmittedAt());
            appInfo.put("reviewedAt", app.getReviewedAt());
            result.add(appInfo);
        }
        return result;
    }

    @PutMapping("/{id}/reject")
    public MembershipApplication rejectApplication(
            @PathVariable Integer id, @RequestParam String rejectReason) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        MembershipApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("报名记录不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(application.getClubId())) {
                throw new RuntimeException("您没有权限审核该社团的报名");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限审核报名");
        }

        application.setStatus(2);
        application.setRejectReason(rejectReason);
        application.setReviewedAt(LocalDateTime.now());
        applicationMapper.updateById(application);
        return application;
    }

    @PutMapping("/{id}/approve")
    public MembershipApplication approveApplication(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        MembershipApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("报名记录不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(application.getClubId())) {
                throw new RuntimeException("您没有权限审核该社团的报名");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限审核报名");
        }

        application.setStatus(1);
        application.setReviewedAt(LocalDateTime.now());
        applicationMapper.updateById(application);

        if (!clubMemberMapper.existsByClubIdAndUserId(
                application.getClubId().longValue(), application.getUserId().longValue())) {
            ClubMember member = new ClubMember();
            member.setClubId(application.getClubId().longValue());
            member.setUserId(application.getUserId().longValue());
            member.setRole(1);
            member.setStatus(1);
            member.setJoinTime(LocalDateTime.now());
            member.setCreatedAt(LocalDateTime.now());
            member.setUpdatedAt(LocalDateTime.now());
            clubMemberMapper.insert(member);
        }

        return application;
    }

    @GetMapping("/club/{clubId}/pending")
    public List<Map<String, Object>> getPendingApplications(@PathVariable Integer clubId) {
        List<MembershipApplication> applications =
                applicationMapper.selectByClubIdAndStatus(clubId, 0);

        List<Map<String, Object>> result = new ArrayList<>();
        for (MembershipApplication app : applications) {
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("id", app.getId());
            appInfo.put("userId", app.getUserId());
            User u = userMapper.selectById(app.getUserId());
            if (u != null) {
                appInfo.put("studentName", u.getRealName());
                appInfo.put("studentStudentId", u.getStudentId());
                appInfo.put("studentAvatar", u.getAvatar());
            }
            appInfo.put("selfIntro", app.getSelfIntro());
            appInfo.put("phone", app.getPhone());
            appInfo.put("status", app.getStatus());
            appInfo.put("submittedAt", app.getSubmittedAt());
            result.add(appInfo);
        }
        return result;
    }

    @PutMapping("/{id}/status")
    public MembershipApplication updateApplicationStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        MembershipApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("报名记录不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(application.getClubId())) {
                throw new RuntimeException("您没有权限审核该社团的报名");
            }
        } else if (role != 1) {
            throw new RuntimeException("您没有权限审核报名");
        }

        if (status != 1 && status != 2) {
            throw new RuntimeException("无效的状态值，只能是1（通过）或2（拒绝）");
        }

        if (status == 2 && (rejectReason == null || rejectReason.isEmpty())) {
            throw new RuntimeException("拒绝时必须提供拒绝理由");
        }

        application.setStatus(status);
        if (status == 2) {
            application.setRejectReason(rejectReason);
        }
        application.setReviewedAt(LocalDateTime.now());

        applicationMapper.updateById(application);

        if (status == 1) {
            if (!clubMemberMapper.existsByClubIdAndUserId(
                    application.getClubId().longValue(), application.getUserId().longValue())) {
                ClubMember member = new ClubMember();
                member.setClubId(application.getClubId().longValue());
                member.setUserId(application.getUserId().longValue());
                member.setRole(1);
                member.setStatus(1);
                member.setJoinTime(LocalDateTime.now());
                member.setCreatedAt(LocalDateTime.now());
                member.setUpdatedAt(LocalDateTime.now());
                clubMemberMapper.insert(member);
            }
        }

        return application;
    }

    @GetMapping("/check")
    public Map<String, Object> checkMembership(@RequestParam Integer clubId) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        boolean isMember =
                clubMemberMapper.existsByClubIdAndUserId(clubId.longValue(), userId.longValue());

        Map<String, Object> result = new HashMap<>();
        result.put("isMember", isMember);
        return result;
    }
}
