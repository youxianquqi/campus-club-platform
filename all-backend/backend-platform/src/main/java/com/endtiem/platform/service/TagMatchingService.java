/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 通用标签匹配服务 实现基于 Jaccard 相似度的标签匹配算法 */
@Slf4j
@Service
public class TagMatchingService {

    /**
     * 计算两个标签集合的 Jaccard 相似度 Jaccard = |A ∩ B| / |A ∪ B|
     *
     * @param set1 标签集合1
     * @param set2 标签集合2
     * @return 相似度分数 [0.0, 1.0]
     */
    public double calculateJaccardSimilarity(Set<Integer> set1, Set<Integer> set2) {
        if (set1 == null || set2 == null) {
            return 0.0;
        }
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0;
        }

        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * 计算两个标签集合的加权 Jaccard 相似度
     *
     * @param set1 标签集合1
     * @param set2 标签集合2
     * @param weights1 集合1的标签权重
     * @param weights2 集合2的标签权重
     * @param defaultWeight 默认权重
     * @return 加权相似度分数 [0.0, 1.0]
     */
    public double calculateWeightedJaccardSimilarity(
            Set<Integer> set1,
            Set<Integer> set2,
            java.util.Map<Integer, Double> weights1,
            java.util.Map<Integer, Double> weights2,
            double defaultWeight) {

        if (set1 == null || set2 == null) {
            return 0.0;
        }

        double intersectionWeight = 0.0;
        double totalWeight = 0.0;

        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        for (Integer tagId : union) {
            double w1 =
                    weights1 != null && weights1.containsKey(tagId)
                            ? weights1.get(tagId)
                            : defaultWeight;
            double w2 =
                    weights2 != null && weights2.containsKey(tagId)
                            ? weights2.get(tagId)
                            : defaultWeight;

            boolean inSet1 = set1.contains(tagId);
            boolean inSet2 = set2.contains(tagId);

            if (inSet1 && inSet2) {
                intersectionWeight += Math.min(w1, w2);
            }

            totalWeight += Math.max(w1, w2);
        }

        return totalWeight > 0 ? intersectionWeight / totalWeight : 0.0;
    }

    /**
     * 计算标签匹配数量
     *
     * @param userTags 用户标签集合
     * @param entityTags 实体标签集合
     * @return 匹配的标签数量
     */
    public int countMatches(Set<Integer> userTags, Set<Integer> entityTags) {
        if (userTags == null || entityTags == null) {
            return 0;
        }

        Set<Integer> intersection = new HashSet<>(userTags);
        intersection.retainAll(entityTags);
        return intersection.size();
    }

    /**
     * 计算用户与单个实体的匹配分数
     *
     * @param userTagIds 用户标签ID集合
     * @param entityTagIds 实体标签ID集合
     * @param similarityWeight 相似度权重 (0.0-1.0)
     * @param matchCountWeight 匹配数量权重
     * @param maxScore 最大分数
     * @return 综合匹配分数
     */
    public double calculateMatchScore(
            Set<Integer> userTagIds,
            Set<Integer> entityTagIds,
            double similarityWeight,
            double matchCountWeight,
            double maxScore) {

        if (userTagIds == null || userTagIds.isEmpty()) {
            return 0.0;
        }

        double similarity = calculateJaccardSimilarity(userTagIds, entityTagIds);
        int matchCount = countMatches(userTagIds, entityTagIds);

        double score = (similarity * similarityWeight) + (matchCount * matchCountWeight);
        return Math.min(score, maxScore);
    }

    /**
     * 解析逗号分隔的标签字符串为标签ID集合 支持中文逗号、英文逗号、分号、分号分隔 同时支持标签名称和标签ID两种格式
     *
     * @param tagsString 逗号分隔的标签字符串（可以是 "编程,科技创新" 或 "1,13"）
     * @param tagNameToId 标签名到ID的映射
     * @return 标签ID集合
     */
    public Set<Integer> parseTagsString(
            String tagsString, java.util.Map<String, Integer> tagNameToId) {
        Set<Integer> tagIds = new HashSet<>();
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return tagIds;
        }

        String[] tagParts = tagsString.split("[,，;；,]");
        for (String part : tagParts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (tagNameToId.containsKey(trimmed)) {
                tagIds.add(tagNameToId.get(trimmed));
            } else {
                try {
                    int tagId = Integer.parseInt(trimmed);
                    tagIds.add(tagId);
                } catch (NumberFormatException e) {
                }
            }
        }
        return tagIds;
    }

    /**
     * 解析逗号分隔的标签字符串为标签名称集合 支持中文逗号、英文逗号、分号、分号分隔 同时支持标签名称和标签ID两种格式
     *
     * @param tagsString 逗号分隔的标签字符串（可以是 "编程,科技创新" 或 "1,13"）
     * @param tagIdToName 标签ID到名称的映射
     * @return 标签名称集合
     */
    public Set<String> parseTagsStringToNames(
            String tagsString, java.util.Map<Integer, String> tagIdToName) {
        Set<String> tagNames = new HashSet<>();
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return tagNames;
        }

        String[] tagParts = tagsString.split("[,，;；,]");
        for (String part : tagParts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (tagIdToName.containsKey(trimmed)) {
                tagNames.add(tagIdToName.get(trimmed));
            } else {
                try {
                    int tagId = Integer.parseInt(trimmed);
                    if (tagIdToName.containsKey(tagId)) {
                        tagNames.add(tagIdToName.get(tagId));
                    } else {
                        tagNames.add(trimmed);
                    }
                } catch (NumberFormatException e) {
                    tagNames.add(trimmed);
                }
            }
        }
        return tagNames;
    }

    /**
     * 解析逗号分隔的标签字符串为标签名称集合（简单版本） 不做ID到名称的转换，直接返回原始字符串
     *
     * @param tagsString 逗号分隔的标签字符串
     * @return 标签名称集合
     */
    public Set<String> parseTagsStringToNames(String tagsString) {
        Set<String> tagNames = new HashSet<>();
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return tagNames;
        }

        String[] tags = tagsString.split("[,，;；,]");
        for (String tag : tags) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                tagNames.add(trimmed);
            }
        }
        return tagNames;
    }

    /**
     * 获取两个集合的交集
     *
     * @param set1 集合1
     * @param set2 集合2
     * @return 交集
     */
    public Set<Integer> getIntersection(Set<Integer> set1, Set<Integer> set2) {
        if (set1 == null || set2 == null) {
            return new HashSet<>();
        }
        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }

    /**
     * 获取两个集合的并集
     *
     * @param set1 集合1
     * @param set2 集合2
     * @return 并集
     */
    public Set<Integer> getUnion(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> union = new HashSet<>();
        if (set1 != null) {
            union.addAll(set1);
        }
        if (set2 != null) {
            union.addAll(set2);
        }
        return union;
    }
}
