/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubTag;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.club.service.ClubService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 社团服务实现类 实现社团相关的业务逻辑
 *
 * @since 2026-03-27
 */
@Service
public class ClubServiceImpl extends ServiceImpl<ClubMapper, Club> implements ClubService {

    private final ClubMapper clubMapper;
    private final ClubTagMapper clubTagMapper;

    public ClubServiceImpl(ClubMapper clubMapper, ClubTagMapper clubTagMapper) {
        this.clubMapper = clubMapper;
        this.clubTagMapper = clubTagMapper;
    }

    @Override
    public Club getById(Long id) {
        if (id == null) {
            return null;
        }
        return clubMapper.selectById(id);
    }

    @Override
    public List<Club> listAll() {
        return clubMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(Club club) {
        if (club == null) {
            throw new IllegalArgumentException("社团信息不能为空");
        }
        return clubMapper.insert(club) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Club club) {
        if (club == null || club.getId() == null) {
            throw new IllegalArgumentException("社团信息或ID不能为空");
        }
        Club existing = clubMapper.selectById(club.getId());
        if (existing == null) {
            throw new IllegalStateException("社团不存在");
        }
        return clubMapper.updateById(club) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("社团ID不能为空");
        }
        Club existing = clubMapper.selectById(id);
        if (existing == null) {
            throw new IllegalStateException("社团不存在");
        }
        return clubMapper.deleteById(id) > 0;
    }

    @Override
    public List<ClubTag> getTagsByClubId(Long clubId) {
        if (clubId == null) {
            throw new IllegalArgumentException("社团ID不能为空");
        }
        return clubTagMapper.selectByClubId(clubId.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTag(Long clubId, Long tagId) {
        if (clubId == null || tagId == null) {
            throw new IllegalArgumentException("社团ID和标签ID不能为空");
        }
        ClubTag clubTag = new ClubTag();
        clubTag.setClubId(clubId.intValue());
        clubTag.setTagId(tagId.intValue());
        return clubTagMapper.insert(clubTag) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeTag(Long clubId, Long tagId) {
        if (clubId == null || tagId == null) {
            throw new IllegalArgumentException("社团ID和标签ID不能为空");
        }
        return clubTagMapper.deleteByClubIdAndTagId(clubId.intValue(), tagId.intValue()) > 0;
    }
}
