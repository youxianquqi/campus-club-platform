/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.controller;

import com.endtiem.common.entity.InviteCode;
import com.endtiem.common.mapper.InviteCodeMapper;
import com.endtiem.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/invite-codes")
public class InviteCodeController {

    private final InviteCodeMapper inviteCodeMapper;
    private final JwtUtil jwtUtil;

    public InviteCodeController(InviteCodeMapper inviteCodeMapper, JwtUtil jwtUtil) {
        this.inviteCodeMapper = inviteCodeMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public Map<String, Object> createInviteCode(@RequestBody Map<String, Object> request) {
        Integer userId = requireUserIdFromToken();
        Integer clubId = request.get("clubId") != null ? toInteger(request.get("clubId")) : null;
        Integer maxUses = request.get("maxUses") != null ? toInteger(request.get("maxUses")) : 10;
        String expiresAtStr =
                request.get("expiresAt") != null ? request.get("expiresAt").toString() : null;

        String code = generateUniqueCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = null;
        if (expiresAtStr != null && !expiresAtStr.isEmpty()) {
            try {
                expiresAt = LocalDateTime.parse(expiresAtStr);
            } catch (Exception e) {
                expiresAt = now.plusDays(30);
            }
        } else {
            expiresAt = now.plusDays(30);
        }

        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(code);
        inviteCode.setCreatorId(userId);
        inviteCode.setClubId(clubId);
        inviteCode.setMaxUses(maxUses);
        inviteCode.setUsedCount(0);
        inviteCode.setExpiresAt(expiresAt);
        inviteCode.setStatus(1);
        inviteCode.setCreatedAt(now);
        inviteCodeMapper.insert(inviteCode);

        Map<String, Object> result = new HashMap<>();
        result.put("id", inviteCode.getId());
        result.put("code", code);
        result.put("maxUses", maxUses);
        result.put("expiresAt", expiresAt);
        result.put("createdAt", now);
        return result;
    }

    @GetMapping("/my")
    public List<Map<String, Object>> getMyInviteCodes() {
        Integer userId = requireUserIdFromToken();
        List<InviteCode> codes = inviteCodeMapper.selectValidByCreatorId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (InviteCode code : codes) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", code.getId());
            row.put("code", code.getCode());
            row.put("clubId", code.getClubId());
            row.put("maxUses", code.getMaxUses());
            row.put("usedCount", code.getUsedCount());
            row.put("expiresAt", code.getExpiresAt());
            row.put("status", code.getStatus());
            row.put("createdAt", code.getCreatedAt());
            result.add(row);
        }
        return result;
    }

    @GetMapping("/verify/{code}")
    public Map<String, Object> verifyInviteCode(@PathVariable String code) {
        InviteCode inviteCode = inviteCodeMapper.selectByCode(code);
        Map<String, Object> result = new HashMap<>();
        if (inviteCode == null) {
            result.put("valid", false);
            result.put("message", "邀请码不存在");
            return result;
        }
        if (inviteCode.getStatus() != 1) {
            result.put("valid", false);
            result.put("message", "邀请码已失效");
            return result;
        }
        if (inviteCode.getExpiresAt() != null
                && inviteCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            result.put("valid", false);
            result.put("message", "邀请码已过期");
            return result;
        }
        if (inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            result.put("valid", false);
            result.put("message", "邀请码使用次数已用完");
            return result;
        }
        result.put("valid", true);
        result.put("message", "邀请码有效");
        result.put("clubId", inviteCode.getClubId());
        result.put("creatorId", inviteCode.getCreatorId());
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteInviteCode(@PathVariable Integer id) {
        Integer userId = requireUserIdFromToken();
        InviteCode inviteCode = inviteCodeMapper.selectById(id);
        if (inviteCode == null) {
            throw new RuntimeException("邀请码不存在");
        }
        if (!inviteCode.getCreatorId().equals(userId)) {
            throw new RuntimeException("只能删除自己的邀请码");
        }
        inviteCode.setStatus(0);
        inviteCodeMapper.updateById(inviteCode);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (inviteCodeMapper.selectByCode(code) != null);
        return code;
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

    private static Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(o.toString());
    }
}
