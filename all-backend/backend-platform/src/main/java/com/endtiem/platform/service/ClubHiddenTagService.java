/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service;

import com.endtiem.club.entity.ClubTag;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.platform.entity.Tag;
import com.endtiem.platform.mapper.TagMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ClubHiddenTagService {

    private final ClubTagMapper clubTagMapper;
    private final TagMapper tagMapper;

    public ClubHiddenTagService(ClubTagMapper clubTagMapper, TagMapper tagMapper) {
        this.clubTagMapper = clubTagMapper;
        this.tagMapper = tagMapper;
    }

    public List<Map<String, Object>> getHiddenTags(Integer clubId) {
        log.debug("getHiddenTags clubId={}", clubId);
        List<ClubTag> hiddenTags = clubTagMapper.selectByClubIdAndIsHidden(clubId, true);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClubTag clubTag : hiddenTags) {
            Map<String, Object> tagInfo = new HashMap<>();
            tagInfo.put("id", clubTag.getId());
            tagInfo.put("clubId", clubTag.getClubId());
            tagInfo.put("tagId", clubTag.getTagId());
            tagInfo.put("isHidden", clubTag.getIsHidden());
            tagInfo.put(
                    "weight", clubTag.getWeight() != null ? clubTag.getWeight() : BigDecimal.ONE);
            tagInfo.put("description", clubTag.getDescription());

            Tag tag = tagMapper.selectById(clubTag.getTagId());
            if (tag != null) {
                tagInfo.put("tagName", tag.getName());
            }

            result.add(tagInfo);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class, readOnly = false)
    public boolean addHiddenTag(
            Integer clubId, Integer tagId, BigDecimal weight, String description) {
        var existing = clubTagMapper.selectByClubIdAndTagId(clubId, tagId);

        if (existing.isPresent()) {
            ClubTag clubTag = existing.get();
            clubTag.setIsHidden(true);
            if (weight != null) {
                clubTag.setWeight(weight);
            }
            if (description != null) {
                clubTag.setDescription(description);
            }
            clubTagMapper.updateById(clubTag);
            return true;
        }

        ClubTag newClubTag = new ClubTag();
        newClubTag.setClubId(clubId);
        newClubTag.setTagId(tagId);
        newClubTag.setIsHidden(true);
        newClubTag.setWeight(weight != null ? weight : BigDecimal.ONE);
        newClubTag.setDescription(description);
        clubTagMapper.insert(newClubTag);
        return true;
    }

    @Transactional(rollbackFor = Exception.class, readOnly = false)
    public boolean addHiddenTagByName(
            Integer clubId, String tagName, BigDecimal weight, String description) {
        var tagOpt = tagMapper.selectByName(tagName);

        Tag tag;
        if (tagOpt.isEmpty()) {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            tagMapper.insert(newTag);
            tag = newTag;
        } else {
            tag = tagOpt.get();
        }

        return addHiddenTag(clubId, tag.getId(), weight, description);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = false)
    public boolean removeHiddenTag(Integer clubId, Integer tagId) {
        var existing = clubTagMapper.selectByClubIdAndTagIdAndIsHidden(clubId, tagId, true);

        if (existing.isPresent()) {
            ClubTag clubTag = existing.get();
            clubTag.setIsHidden(false);
            clubTagMapper.updateById(clubTag);
            return true;
        }

        return false;
    }

    @Transactional(rollbackFor = Exception.class, readOnly = false)
    public boolean removeHiddenTagByName(Integer clubId, String tagName) {
        return tagMapper
                .selectByName(tagName)
                .map(tag -> removeHiddenTag(clubId, tag.getId()))
                .orElse(false);
    }

    public List<Map<String, Object>> getVisibleTags(Integer clubId) {
        log.debug("getVisibleTags clubId={}", clubId);
        List<ClubTag> visibleTags = clubTagMapper.selectByClubIdAndIsHidden(clubId, false);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClubTag clubTag : visibleTags) {
            Map<String, Object> tagInfo = new HashMap<>();
            tagInfo.put("id", clubTag.getId());
            tagInfo.put("clubId", clubTag.getClubId());
            tagInfo.put("tagId", clubTag.getTagId());
            tagInfo.put("isHidden", clubTag.getIsHidden());
            tagInfo.put(
                    "weight", clubTag.getWeight() != null ? clubTag.getWeight() : BigDecimal.ONE);
            tagInfo.put("description", clubTag.getDescription());

            Tag tag = tagMapper.selectById(clubTag.getTagId());
            if (tag != null) {
                tagInfo.put("tagName", tag.getName());
            }

            result.add(tagInfo);
        }

        return result;
    }

    public List<Integer> getAllTagIdsForMatching(Integer clubId) {
        List<ClubTag> allTags = clubTagMapper.selectByClubId(clubId);
        List<Integer> tagIds = new ArrayList<>();
        for (ClubTag clubTag : allTags) {
            tagIds.add(clubTag.getTagId());
        }
        return tagIds;
    }
}
