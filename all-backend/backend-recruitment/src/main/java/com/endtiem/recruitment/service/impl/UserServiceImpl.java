/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.recruitment.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("recruitmentUserServiceImpl")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, ClubMapper clubMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.clubMapper = clubMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public User getById(Integer id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }

    @Override
    @Deprecated
    public User getByRelatedId(Integer relatedId) {
        return getById(relatedId);
    }

    @Override
    public User getCurrentUser(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);
        if (relatedId == null) {
            throw new IllegalStateException("无法从Token中获取用户信息");
        }
        User user = userMapper.selectById(relatedId);
        if (user == null) {
            throw new IllegalStateException("用户信息不存在");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateInfo(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("用户信息或ID不能为空");
        }
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new IllegalStateException("用户信息不存在");
        }
        userMapper.updateById(user);
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateAvatar(String token, String avatar) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("头像URL不能为空");
        }
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);
        User user = userMapper.selectById(relatedId);
        if (user == null) {
            throw new IllegalStateException("用户信息不存在");
        }
        user.setAvatar(avatar);
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setRecommendBoost(String token, Integer clubId) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        if (clubId == null) {
            throw new IllegalArgumentException("社团ID不能为空");
        }
        if (clubMapper.selectById(clubId) == null) {
            throw new IllegalStateException("社团不存在");
        }
        Integer relatedId = jwtUtil.getRelatedIdFromToken(token);
        User user = userMapper.selectById(relatedId);
        if (user == null) {
            throw new IllegalStateException("用户信息不存在");
        }
        user.setRecommendBoostClubId(clubId);
        return userMapper.updateById(user) > 0;
    }
}
