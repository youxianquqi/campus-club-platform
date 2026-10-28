/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.entity.ClubMember;
import com.endtiem.recruitment.mapper.ClubMemberMapper;
import com.endtiem.recruitment.service.ClubMemberService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClubMemberServiceImpl extends ServiceImpl<ClubMemberMapper, ClubMember>
        implements ClubMemberService {

    private final ClubMemberMapper clubMemberMapper;
    private final ClubMapper clubMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public ClubMemberServiceImpl(
            ClubMemberMapper clubMemberMapper,
            ClubMapper clubMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil) {
        this.clubMemberMapper = clubMemberMapper;
        this.clubMapper = clubMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public List<Map<String, Object>> getMembersByClubId(Long clubId) {
        if (clubId == null) {
            throw new IllegalArgumentException("社团ID不能为空");
        }
        List<ClubMember> members = clubMemberMapper.selectByClubId(clubId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClubMember member : members) {
            Map<String, Object> memberInfo = toMemberMap(member);
            result.add(memberInfo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubMember joinClub(String token, Long clubId) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        if (clubId == null) {
            throw new IllegalArgumentException("社团ID不能为空");
        }
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);
        Long userId = relatedId.longValue();

        if (clubMemberMapper.existsByClubIdAndUserId(clubId, userId)) {
            throw new IllegalStateException("您已经是该社团成员");
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
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMember(String token, Long memberId) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("成员ID不能为空");
        }
        Integer role = jwtUtil.getRoleFromToken(token);

        ClubMember member = clubMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalStateException("成员不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(member.getClubId().intValue())) {
                throw new IllegalStateException("您没有权限移除该成员");
            }
        } else if (role != 1) {
            throw new IllegalStateException("您没有权限移除成员");
        }

        member.setStatus(3);
        member.setUpdatedAt(LocalDateTime.now());
        return clubMemberMapper.updateById(member) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMemberByClubAndUser(String token, Long clubId, Long userId) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        if (clubId == null || userId == null) {
            throw new IllegalArgumentException("社团ID或用户ID不能为空");
        }
        Integer role = jwtUtil.getRoleFromToken(token);

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(clubId.intValue())) {
                throw new IllegalStateException("您没有权限移除该成员");
            }
        } else if (role != 1) {
            throw new IllegalStateException("您没有权限移除成员");
        }

        List<ClubMember> members = clubMemberMapper.selectByClubId(clubId);
        for (ClubMember member : members) {
            if (member.getUserId().equals(userId)) {
                member.setStatus(3);
                member.setUpdatedAt(LocalDateTime.now());
                clubMemberMapper.updateById(member);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberRole(String token, Long memberId, Integer newRole) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("成员ID不能为空");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("新角色不能为空");
        }
        Integer role = jwtUtil.getRoleFromToken(token);

        ClubMember member = clubMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalStateException("成员不存在");
        }

        if (role >= 2) {
            Integer managedClubId = role - 1;
            if (!managedClubId.equals(member.getClubId().intValue())) {
                throw new IllegalStateException("您没有权限更新该成员角色");
            }
        } else if (role != 1) {
            throw new IllegalStateException("您没有权限更新成员角色");
        }

        member.setRole(newRole);
        member.setUpdatedAt(LocalDateTime.now());
        return clubMemberMapper.updateById(member) > 0;
    }

    private Map<String, Object> toMemberMap(ClubMember member) {
        Map<String, Object> memberInfo = new java.util.HashMap<>();
        memberInfo.put("id", member.getId());
        memberInfo.put("clubId", member.getClubId());
        memberInfo.put("userId", member.getUserId());
        memberInfo.put("role", member.getRole());
        memberInfo.put("joinTime", member.getJoinTime());
        memberInfo.put("status", member.getStatus());

        User u = userMapper.selectById(member.getUserId().intValue());
        if (u != null) {
            memberInfo.put("studentId", u.getStudentId());
            memberInfo.put("name", u.getRealName());
            memberInfo.put("nickname", u.getNickname());
            memberInfo.put("avatar", u.getAvatar());
            memberInfo.put("college", u.getCollege());
            memberInfo.put("major", u.getMajor());
        }

        return memberInfo;
    }
}
