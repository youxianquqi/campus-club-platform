/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service.provider;

import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubTag;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.platform.mapper.TagMapper;
import com.endtiem.platform.service.EntityTagProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 社团标签提供者 适配现有 club_tag 关联表 */
@Slf4j
@Component
public class ClubTagProvider implements EntityTagProvider<Club> {

    public static final String ENTITY_TYPE = "CLUB";

    private final ClubTagMapper clubTagMapper;
    private final TagMapper tagMapper;

    public ClubTagProvider(ClubTagMapper clubTagMapper, TagMapper tagMapper) {
        this.clubTagMapper = clubTagMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    public Set<Integer> getTagIds(Club entity) {
        if (entity == null || entity.getId() == null) {
            return Collections.emptySet();
        }
        return clubTagMapper.selectByClubId(entity.getId()).stream()
                .map(ClubTag::getTagId)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getTagNames(Club entity) {
        if (entity == null || entity.getId() == null) {
            return Collections.emptySet();
        }
        List<ClubTag> clubTags = clubTagMapper.selectByClubId(entity.getId());
        Map<Integer, String> tagIdToName =
                tagMapper.selectList(null).stream()
                        .collect(Collectors.toMap(tag -> tag.getId(), tag -> tag.getName()));

        return clubTags.stream()
                .map(ct -> tagIdToName.get(ct.getTagId()))
                .filter(name -> name != null)
                .collect(Collectors.toSet());
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public boolean isValid(Club entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        List<ClubTag> clubTags = clubTagMapper.selectByClubId(entity.getId());
        return clubTags != null && !clubTags.isEmpty();
    }

    public Set<Integer> getVisibleTagIds(Club entity) {
        if (entity == null || entity.getId() == null) {
            return Collections.emptySet();
        }
        return clubTagMapper.selectByClubIdAndIsHidden(entity.getId(), false).stream()
                .map(ClubTag::getTagId)
                .collect(Collectors.toSet());
    }

    public Set<Integer> getHiddenTagIds(Club entity) {
        if (entity == null || entity.getId() == null) {
            return Collections.emptySet();
        }
        return clubTagMapper.selectByClubIdAndIsHidden(entity.getId(), true).stream()
                .map(ClubTag::getTagId)
                .collect(Collectors.toSet());
    }

    public Map<Integer, Double> getTagWeights(Club entity) {
        if (entity == null || entity.getId() == null) {
            return Collections.emptyMap();
        }
        return clubTagMapper.selectByClubId(entity.getId()).stream()
                .filter(ct -> ct.getWeight() != null)
                .collect(Collectors.toMap(ClubTag::getTagId, ct -> ct.getWeight().doubleValue()));
    }
}
