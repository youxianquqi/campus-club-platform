/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service.provider;

import com.endtiem.club.entity.Vote;
import com.endtiem.platform.mapper.TagMapper;
import com.endtiem.platform.service.EntityTagProvider;
import com.endtiem.platform.service.TagMatchingService;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 投票标签提供者 适配 vote.tags 字符串字段（逗号分隔格式） */
@Slf4j
@Component
public class VoteTagProvider implements EntityTagProvider<Vote> {

    public static final String ENTITY_TYPE = "VOTE";

    private final TagMapper tagMapper;
    private final TagMatchingService tagMatchingService;

    public VoteTagProvider(TagMapper tagMapper, TagMatchingService tagMatchingService) {
        this.tagMapper = tagMapper;
        this.tagMatchingService = tagMatchingService;
    }

    @Override
    public Set<Integer> getTagIds(Vote entity) {
        if (entity == null || entity.getTags() == null || entity.getTags().trim().isEmpty()) {
            return Collections.emptySet();
        }
        Map<String, Integer> tagNameToId = buildTagNameToIdMap();
        return tagMatchingService.parseTagsString(entity.getTags(), tagNameToId);
    }

    @Override
    public Set<String> getTagNames(Vote entity) {
        if (entity == null || entity.getTags() == null || entity.getTags().trim().isEmpty()) {
            return Collections.emptySet();
        }
        Map<Integer, String> tagIdToName = buildTagIdToNameMap();
        return tagMatchingService.parseTagsStringToNames(entity.getTags(), tagIdToName);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public boolean isValid(Vote entity) {
        if (entity == null || entity.getTags() == null || entity.getTags().trim().isEmpty()) {
            return false;
        }
        Set<String> tagNames = getTagNames(entity);
        return tagNames != null && !tagNames.isEmpty();
    }

    private Map<String, Integer> buildTagNameToIdMap() {
        return tagMapper.selectList(null).stream()
                .collect(Collectors.toMap(tag -> tag.getName(), tag -> tag.getId()));
    }

    private Map<Integer, String> buildTagIdToNameMap() {
        return tagMapper.selectList(null).stream()
                .collect(Collectors.toMap(tag -> tag.getId(), tag -> tag.getName()));
    }
}
