/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service;

import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.Club;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.platform.dto.ClubRecommendation;
import com.endtiem.platform.entity.RecommendationRule;
import com.endtiem.platform.mapper.RecommendationRuleMapper;
import com.endtiem.platform.service.provider.ClubTagProvider;
import com.endtiem.recruitment.entity.UserTag;
import com.endtiem.recruitment.mapper.UserTagMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final UserTagMapper userTagMapper;
    private final ClubTagMapper clubTagMapper;
    private final ClubMapper clubMapper;
    private final RecommendationRuleMapper recommendationRuleMapper;
    private final UserMapper userMapper;
    private final TagMatchingService tagMatchingService;
    private final ClubTagProvider clubTagProvider;

    public RecommendationService(
            UserTagMapper userTagMapper,
            ClubTagMapper clubTagMapper,
            ClubMapper clubMapper,
            RecommendationRuleMapper recommendationRuleMapper,
            UserMapper userMapper,
            TagMatchingService tagMatchingService,
            ClubTagProvider clubTagProvider) {
        this.userTagMapper = userTagMapper;
        this.clubTagMapper = clubTagMapper;
        this.clubMapper = clubMapper;
        this.recommendationRuleMapper = recommendationRuleMapper;
        this.userMapper = userMapper;
        this.tagMatchingService = tagMatchingService;
        this.clubTagProvider = clubTagProvider;
    }

    public List<ClubRecommendation> recommendForStudent(long userId) {
        return recommendForStudent(userId, null, null);
    }

    public List<ClubRecommendation> recommendForStudent(long userId, String qqBoostKeyword) {
        return recommendForStudent(userId, qqBoostKeyword, null);
    }

    public List<ClubRecommendation> recommendForStudent(
            long userId, String qqBoostKeyword, Integer qqBoostClubId) {
        log.debug("recommendForUser userId={}", userId);
        RuleWeights w = resolveWeights();

        Set<Integer> userTagIds = getUserTagIds((int) userId);

        String qqNorm = normalizeQq(qqBoostKeyword);

        Integer effectiveBoostClubId = resolveQqBoostClubId((int) userId, qqBoostClubId);

        List<Club> clubs = clubMapper.selectList(null);
        List<ClubScore> scored = new ArrayList<>();
        for (Club club : clubs) {
            double score = computeTotalScore(club, userTagIds, qqNorm, w, effectiveBoostClubId);
            scored.add(new ClubScore(club, score));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        double maxTotal = w.maxTotalForSimilarity();
        return scored.stream()
                .map(cs -> toRecommendation(cs.club, cs.score, maxTotal))
                .collect(Collectors.toList());
    }

    private Set<Integer> getUserTagIds(int userId) {
        List<UserTag> userTags = userTagMapper.selectByUserId(userId);
        return userTags.stream().map(UserTag::getTagId).collect(Collectors.toSet());
    }

    private Integer resolveQqBoostClubId(int userId, Integer requestClubId) {
        User u = userMapper.selectById(userId);
        if (u != null && u.getRecommendBoostClubId() != null) {
            return u.getRecommendBoostClubId();
        }
        return requestClubId;
    }

    private RuleWeights resolveWeights() {
        return recommendationRuleMapper
                .selectFirstByOrderByIdAsc()
                .map(RuleWeights::fromEntity)
                .orElse(RuleWeights.defaults());
    }

    private double computeTotalScore(
            Club club,
            Set<Integer> userTagIds,
            String qqNormKeyword,
            RuleWeights w,
            Integer qqBoostClubId) {
        double tagPart = jaccardVisibleTags(userTagIds, club) * w.maxTagScore;
        double hiddenPart = hiddenTagMatchScore(userTagIds, club, w);
        double qqPart = 0.0;
        if (qqBoostClubId != null && qqBoostClubId.equals(club.getId())) {
            qqPart = w.qqBoostScore;
        } else if (qqBoostMatches(club, qqNormKeyword)) {
            qqPart = w.qqBoostScore;
        }
        return tagPart + hiddenPart + qqPart;
    }

    private double jaccardVisibleTags(Set<Integer> userTagIds, Club club) {
        Set<Integer> visibleClubTags = clubTagProvider.getVisibleTagIds(club);
        return tagMatchingService.calculateJaccardSimilarity(userTagIds, visibleClubTags);
    }

    private double hiddenTagMatchScore(Set<Integer> userTagIds, Club club, RuleWeights w) {
        if (userTagIds.isEmpty()) {
            return 0.0;
        }
        Set<Integer> hiddenTagIds = clubTagProvider.getHiddenTagIds(club);
        int matches = tagMatchingService.countMatches(userTagIds, hiddenTagIds);
        return Math.min(w.maxHiddenScore, matches * w.hiddenMatchUnit);
    }

    private static boolean qqBoostMatches(Club club, String qqNormKeyword) {
        if (qqNormKeyword == null || qqNormKeyword.isEmpty()) {
            return false;
        }
        if (club.getQqGroup() == null || club.getQqGroup().trim().isEmpty()) {
            return false;
        }
        String qn = normalizeQq(club.getQqGroup());
        return qn.contains(qqNormKeyword) || qqNormKeyword.contains(qn);
    }

    private static String normalizeQq(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\s+", "");
    }

    private ClubRecommendation toRecommendation(
            Club club, double totalScore, double maxTotalForSimilarity) {
        ClubRecommendation rec = new ClubRecommendation();
        rec.setId(club.getId());
        rec.setName(club.getName());
        rec.setDescription(club.getDescription());
        List<Integer> tagIds = new ArrayList<>(clubTagProvider.getVisibleTagIds(club));
        rec.setTags(tagIds);
        double denom = maxTotalForSimilarity > 0 ? maxTotalForSimilarity : 100.0;
        rec.setSimilarity(Math.min(1.0, Math.min(denom, totalScore) / denom));
        return rec;
    }

    private static final class RuleWeights {
        final double maxTagScore;
        final double qqBoostScore;
        final double hiddenMatchUnit;
        final double maxHiddenScore;

        RuleWeights(
                double maxTagScore,
                double qqBoostScore,
                double hiddenMatchUnit,
                double maxHiddenScore) {
            this.maxTagScore = Math.max(0, maxTagScore);
            this.qqBoostScore = Math.max(0, qqBoostScore);
            this.hiddenMatchUnit = Math.max(0, hiddenMatchUnit);
            this.maxHiddenScore = Math.max(0, maxHiddenScore);
        }

        static RuleWeights defaults() {
            return new RuleWeights(60.0, 20.0, 5.0, 20.0);
        }

        static RuleWeights fromEntity(RecommendationRule r) {
            return new RuleWeights(
                    bd(r.getMaxTagScore(), 60.0),
                    bd(r.getQqBoostScore(), 20.0),
                    bd(r.getHiddenMatchUnit(), 5.0),
                    bd(r.getMaxHiddenScore(), 20.0));
        }

        private static double bd(BigDecimal v, double fallback) {
            if (v == null) {
                return fallback;
            }
            return v.doubleValue();
        }

        double maxTotalForSimilarity() {
            return maxTagScore + qqBoostScore + maxHiddenScore;
        }
    }

    private static final class ClubScore {
        final Club club;
        final double score;

        ClubScore(Club club, double score) {
            this.club = club;
            this.score = score;
        }
    }
}
