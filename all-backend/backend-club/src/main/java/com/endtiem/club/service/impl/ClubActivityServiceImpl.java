/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.endtiem.club.entity.ClubActivity;
import com.endtiem.club.mapper.ClubActivityMapper;
import com.endtiem.club.service.ClubActivityService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClubActivityServiceImpl extends ServiceImpl<ClubActivityMapper, ClubActivity>
        implements ClubActivityService {

    private final ClubActivityMapper clubActivityMapper;

    public ClubActivityServiceImpl(ClubActivityMapper clubActivityMapper) {
        this.clubActivityMapper = clubActivityMapper;
    }

    @Override
    public Page<ClubActivity> pageByStatus(Integer status, int pageNum, int pageSize) {
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        return clubActivityMapper.pageByStatusOrderByCreatedAtDesc(
                status, new Page<>(Math.max(pageNum, 0) + 1L, safeSize));
    }

    @Override
    public ClubActivity getById(Integer id) {
        if (id == null) {
            return null;
        }
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new IllegalStateException("活动不存在");
        }
        return activity;
    }

    @Override
    public List<ClubActivity> listByClubId(Integer clubId) {
        if (clubId == null) {
            throw new IllegalArgumentException("社团ID不能为空");
        }
        return clubActivityMapper.selectByClubIdOrderByCreatedAtDesc(clubId);
    }

    @Override
    public List<ClubActivity> listPendingActivities() {
        return clubActivityMapper.selectByStatusOrderByCreatedAtDesc(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubActivity create(ClubActivity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("活动信息不能为空");
        }
        activity.setStatus(0);
        LocalDateTime now = LocalDateTime.now();
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);
        clubActivityMapper.insert(activity);
        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubActivity update(ClubActivity activity) {
        if (activity == null || activity.getId() == null) {
            throw new IllegalArgumentException("活动信息或ID不能为空");
        }
        ClubActivity existing = clubActivityMapper.selectById(activity.getId());
        if (existing == null) {
            throw new IllegalStateException("活动不存在");
        }
        if (existing.getStatus() != 0 && existing.getStatus() != 2) {
            throw new IllegalStateException("只能编辑待审批或已拒绝的活动");
        }
        activity.setStatus(0);
        activity.setRejectReason(null);
        activity.setUpdatedAt(LocalDateTime.now());
        clubActivityMapper.updateById(activity);
        return clubActivityMapper.selectById(activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubActivity approve(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("活动ID不能为空");
        }
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new IllegalStateException("活动不存在");
        }
        if (activity.getStatus() != 0) {
            throw new IllegalStateException("该活动已审批");
        }
        activity.setStatus(1);
        activity.setReviewedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        clubActivityMapper.updateById(activity);
        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubActivity reject(Integer id, String reason) {
        if (id == null) {
            throw new IllegalArgumentException("活动ID不能为空");
        }
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new IllegalStateException("活动不存在");
        }
        if (activity.getStatus() != 0) {
            throw new IllegalStateException("该活动已审批");
        }
        activity.setStatus(2);
        activity.setRejectReason(reason == null || reason.isEmpty() ? "已驳回" : reason);
        activity.setReviewedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        clubActivityMapper.updateById(activity);
        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("活动ID不能为空");
        }
        ClubActivity activity = clubActivityMapper.selectById(id);
        if (activity == null) {
            throw new IllegalStateException("活动不存在");
        }
        return clubActivityMapper.deleteById(id) > 0;
    }
}
