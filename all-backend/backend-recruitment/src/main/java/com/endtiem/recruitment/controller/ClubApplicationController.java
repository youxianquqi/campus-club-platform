/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubTag;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.dto.ClubApplicationRequest;
import com.endtiem.recruitment.entity.ClubApplication;
import com.endtiem.recruitment.mapper.ClubApplicationMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
@RequestMapping("/api/club-applications")
public class ClubApplicationController {

    private final ClubApplicationMapper clubApplicationMapper;
    private final ClubMapper clubMapper;
    private final ClubTagMapper clubTagMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public ClubApplicationController(
            ClubApplicationMapper clubApplicationMapper,
            ClubMapper clubMapper,
            ClubTagMapper clubTagMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil) {
        this.clubApplicationMapper = clubApplicationMapper;
        this.clubMapper = clubMapper;
        this.clubTagMapper = clubTagMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ClubApplication createApplication(@RequestBody ClubApplicationRequest request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        ClubApplication existing =
                clubApplicationMapper.selectOne(
                        new LambdaQueryWrapper<ClubApplication>()
                                .eq(ClubApplication::getApplicantId, userId)
                                .eq(ClubApplication::getStatus, 0));

        if (existing != null) {
            throw new RuntimeException("您已有待审核的社团申请，请等待审核");
        }

        ClubApplication application = new ClubApplication();
        application.setApplicantId(userId);
        application.setName(request.getName());
        application.setDescription(request.getDescription());
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            application.setTags(
                    request.getTags().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")));
        }
        application.setStatus(0);

        clubApplicationMapper.insert(application);
        return application;
    }

    @GetMapping("/my")
    public List<ClubApplication> getMyApplications() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer userId = jwtUtil.getRelatedIdFromToken(token);

        return clubApplicationMapper.selectByApplicantId(userId);
    }

    @GetMapping("/pending")
    public List<ClubApplication> getPendingApplications() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role != 1) {
            throw new RuntimeException("您没有权限查看社团申请");
        }

        return clubApplicationMapper.selectByStatus(0);
    }

    @GetMapping
    public List<Map<String, Object>> getAllApplications() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);

        if (role != 1) {
            throw new RuntimeException("您没有权限查看社团申请");
        }

        List<ClubApplication> applications = clubApplicationMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClubApplication app : applications) {
            Map<String, Object> appMap = new HashMap<>();
            appMap.put("id", app.getId());
            appMap.put("applicantId", app.getApplicantId());
            appMap.put("name", app.getName());
            appMap.put("description", app.getDescription());
            appMap.put("tags", app.getTags());
            appMap.put("status", app.getStatus());
            appMap.put("rejectReason", app.getRejectReason());
            appMap.put("submittedAt", app.getSubmittedAt());
            appMap.put("reviewedAt", app.getReviewedAt());
            appMap.put("reviewerId", app.getReviewerId());
            appMap.put("createdClubId", app.getCreatedClubId());

            if (app.getApplicantId() != null) {
                User applicant = userMapper.selectById(app.getApplicantId());
                if (applicant != null) {
                    appMap.put("applicantStudentId", applicant.getStudentId());
                    appMap.put("applicantName", applicant.getRealName());
                }
            }

            result.add(appMap);
        }

        return result;
    }

    @PutMapping("/{id}/approve")
    public ClubApplication approveApplication(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        Integer reviewerId = jwtUtil.getRelatedIdFromToken(token);

        if (role != 1) {
            throw new RuntimeException("您没有权限审核社团申请");
        }

        ClubApplication application = clubApplicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        if (application.getStatus() != 0) {
            throw new RuntimeException("该申请已被处理");
        }

        Club club = new Club();
        club.setName(application.getName());
        club.setDescription(application.getDescription());
        club.setAdminId(application.getApplicantId());
        clubMapper.insert(club);

        if (application.getTags() != null && !application.getTags().isEmpty()) {
            String[] tagIds = application.getTags().split(",");
            for (String tagId : tagIds) {
                ClubTag clubTag = new ClubTag();
                clubTag.setClubId(club.getId());
                clubTag.setTagId(Integer.parseInt(tagId.trim()));
                clubTag.setIsHidden(false);
                clubTagMapper.insert(clubTag);
            }
        }

        application.setStatus(1);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewerId(reviewerId);
        application.setCreatedClubId(club.getId());

        clubApplicationMapper.updateById(application);
        return application;
    }

    @PutMapping("/{id}/reject")
    public ClubApplication rejectApplication(
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
        Integer reviewerId = jwtUtil.getRelatedIdFromToken(token);

        if (role != 1) {
            throw new RuntimeException("您没有权限审核社团申请");
        }

        ClubApplication application = clubApplicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        if (application.getStatus() != 0) {
            throw new RuntimeException("该申请已被处理");
        }

        application.setStatus(2);
        application.setRejectReason(rejectReason.isEmpty() ? "已拒绝" : rejectReason);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewerId(reviewerId);

        clubApplicationMapper.updateById(application);
        return application;
    }
}
