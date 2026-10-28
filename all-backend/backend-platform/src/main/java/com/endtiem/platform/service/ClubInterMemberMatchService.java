/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service;

import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubTag;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.ClubTagMapper;
import com.endtiem.platform.entity.Tag;
import com.endtiem.platform.mapper.TagMapper;
import com.endtiem.recruitment.entity.ClubMember;
import com.endtiem.recruitment.entity.UserTag;
import com.endtiem.recruitment.mapper.ClubMemberMapper;
import com.endtiem.recruitment.mapper.UserTagMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 两社团在社成员兴趣标签的跨成员匹配：对双方每个成员的标签集合做两两 Jaccard，取算术平均为匹配度； 并返回成员标签并集层面的共同/独有标签（便于展示）。 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ClubInterMemberMatchService {

    private final ClubMapper clubMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserTagMapper userTagMapper;
    private final TagMapper tagMapper;
    private final ClubTagMapper clubTagMapper;

    public ClubInterMemberMatchService(
            ClubMapper clubMapper,
            ClubMemberMapper clubMemberMapper,
            UserTagMapper userTagMapper,
            TagMapper tagMapper,
            ClubTagMapper clubTagMapper) {
        this.clubMapper = clubMapper;
        this.clubMemberMapper = clubMemberMapper;
        this.userTagMapper = userTagMapper;
        this.tagMapper = tagMapper;
        this.clubTagMapper = clubTagMapper;
    }

    public Map<String, Object> match(int clubId1, int clubId2) {
        log.debug("member match clubId1={} clubId2={}", clubId1, clubId2);
        if (clubId1 == clubId2) {
            throw new RuntimeException("请选择两个不同的社团");
        }
        if (clubMapper.selectById(clubId1) == null || clubMapper.selectById(clubId2) == null) {
            throw new RuntimeException("社团不存在");
        }

        List<Set<Integer>> tagSets1 = loadMemberTagSets(clubId1);
        List<Set<Integer>> tagSets2 = loadMemberTagSets(clubId2);

        int memberCount1 = tagSets1.size();
        int memberCount2 = tagSets2.size();
        int membersWithTags1 = (int) tagSets1.stream().filter(s -> !s.isEmpty()).count();
        int membersWithTags2 = (int) tagSets2.stream().filter(s -> !s.isEmpty()).count();

        CrossClubPairwiseStats stats = computeCrossClubPairwiseStats(tagSets1, tagSets2);
        long pairCount = stats.pairCount;
        int pairsWithPositive = stats.pairsWithPositiveJaccard;
        int matchScore =
                pairCount <= 0 ? 0 : (int) Math.round(stats.averageJaccard * 100.0);

        Set<Integer> union1 = unionAll(tagSets1);
        Set<Integer> union2 = unionAll(tagSets2);
        Set<Integer> interAgg = new HashSet<>(union1);
        interAgg.retainAll(union2);
        Set<Integer> only1 = new HashSet<>(union1);
        only1.removeAll(union2);
        Set<Integer> only2 = new HashSet<>(union2);
        only2.removeAll(union1);
        Set<Integer> unionBoth = new HashSet<>(union1);
        unionBoth.addAll(union2);
        int unionAllBoth = unionBoth.size();

        Map<String, Object> data = new HashMap<>();
        data.put("matchScore", matchScore);
        data.put("memberCount1", memberCount1);
        data.put("memberCount2", memberCount2);
        data.put("membersWithTags1", membersWithTags1);
        data.put("membersWithTags2", membersWithTags2);
        data.put("pairCount", pairCount);
        data.put("pairsWithPositiveJaccard", pairsWithPositive);
        data.put("aggregateIntersectionSize", interAgg.size());
        data.put("aggregateUnionSize1", union1.size());
        data.put("aggregateUnionSize2", union2.size());
        data.put("aggregateUnionAllUnique", unionAllBoth);
        data.put("commonTags", toTagNameList(interAgg));
        data.put("onlyInClub1", toTagNameList(only1));
        data.put("onlyInClub2", toTagNameList(only2));
        return data;
    }

    /**
     * 与 {@link #match(int, int)} 相同：两社在社成员（status=1）的 user_tag，全体两两 Jaccard 的算术平均，范围
     * [0,1]。
     */
    public double averagePairwiseMemberJaccard(int clubId1, int clubId2) {
        if (clubId1 == clubId2) {
            return 0;
        }
        List<Set<Integer>> t1 = loadMemberTagSets(clubId1);
        List<Set<Integer>> t2 = loadMemberTagSets(clubId2);
        return computeCrossClubPairwiseStats(t1, t2).averageJaccard;
    }

    /**
     * 社团详情「相似社团」：与全部社团页「社团匹配」同一套算法，按成员兴趣平均 Jaccard 排序取前 {@code
     * limit} 个。
     */
    public List<Map<String, Object>> rankSimilarClubsByMemberMatch(int clubId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        if (clubMapper.selectById(clubId) == null) {
            throw new RuntimeException("社团不存在");
        }
        List<Club> all = clubMapper.selectList(null);
        List<ScoredClub> scored = new ArrayList<>();
        log.info("查询到 {} 个社团", all.size());
        
        for (Club other : all) {
            if (other.getId().equals(clubId)) {
                continue;
            }
            double sim = averagePairwiseMemberJaccard(clubId, other.getId());
            log.info("社团 {} 与社团 {} 的相似度: {}", clubId, other.getId(), sim);
            if (sim > 0) {
                scored.add(new ScoredClub(other, sim));
            }
        }
        log.info("找到 {} 个相似社团", scored.size());
        scored.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        List<Map<String, Object>> out = new ArrayList<>();
        for (ScoredClub sc : scored) {
            if (out.size() >= safeLimit) {
                break;
            }
            out.add(buildSimilarClubCard(sc.club, sc.similarity));
        }
        return out;
    }

    private Map<String, Object> buildSimilarClubCard(Club club, double similarity01) {
        Map<String, Object> clubMap = new HashMap<>();
        clubMap.put("id", club.getId());
        clubMap.put("name", club.getName());
        clubMap.put("description", club.getDescription());
        clubMap.put("logo", club.getLogoUrl());
        List<ClubTag> clubTags = clubTagMapper.selectByClubId(club.getId());
        List<String> tagNames = new ArrayList<>();
        for (ClubTag ct : clubTags) {
            Tag tag = tagMapper.selectById(ct.getTagId());
            if (tag != null && tag.getName() != null) {
                tagNames.add(tag.getName());
            }
        }
        clubMap.put("tags", tagNames);
        clubMap.put("similarity", similarity01);
        return clubMap;
    }

    private static final class ScoredClub {
        final Club club;
        final double similarity;

        ScoredClub(Club club, double similarity) {
            this.club = club;
            this.similarity = similarity;
        }
    }

    private static final class CrossClubPairwiseStats {
        final double averageJaccard;
        final long pairCount;
        final int pairsWithPositiveJaccard;

        CrossClubPairwiseStats(double averageJaccard, long pairCount, int pairsWithPositiveJaccard) {
            this.averageJaccard = averageJaccard;
            this.pairCount = pairCount;
            this.pairsWithPositiveJaccard = pairsWithPositiveJaccard;
        }
    }

    private static CrossClubPairwiseStats computeCrossClubPairwiseStats(
            List<Set<Integer>> tagSets1, List<Set<Integer>> tagSets2) {
        long pairCount = (long) tagSets1.size() * tagSets2.size();
        if (pairCount <= 0) {
            return new CrossClubPairwiseStats(0, 0, 0);
        }
        double sumJaccard = 0;
        int pairsWithPositive = 0;
        for (Set<Integer> a : tagSets1) {
            for (Set<Integer> b : tagSets2) {
                double j = jaccard(a, b);
                sumJaccard += j;
                if (j > 0) {
                    pairsWithPositive++;
                }
            }
        }
        return new CrossClubPairwiseStats(sumJaccard / pairCount, pairCount, pairsWithPositive);
    }

    private List<Set<Integer>> loadMemberTagSets(int clubId) {
        log.info("加载社团 {} 的成员标签", clubId);
        List<ClubMember> members = clubMemberMapper.selectByClubId((long) clubId);
        log.info("社团 {} 有 {} 个在社成员", clubId, members.size());
        List<Set<Integer>> out = new ArrayList<>();
        for (ClubMember cm : members) {
            if (cm.getUserId() == null) {
                continue;
            }
            int sid = cm.getUserId().intValue();
            if (sid <= 0) {
                continue;
            }
            List<UserTag> rows = userTagMapper.selectByUserId(sid);
            log.info("用户 {} 有 {} 个标签", sid, rows.size());
            Set<Integer> tags =
                    rows.stream()
                            .map(UserTag::getTagId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());
            out.add(tags);
        }
        return out;
    }

    private static double jaccard(Set<Integer> a, Set<Integer> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        int inter = 0;
        for (Integer x : a) {
            if (b.contains(x)) {
                inter++;
            }
        }
        int union = a.size() + b.size() - inter;
        return union == 0 ? 0 : (double) inter / union;
    }

    private static Set<Integer> unionAll(List<Set<Integer>> sets) {
        Set<Integer> u = new HashSet<>();
        for (Set<Integer> s : sets) {
            u.addAll(s);
        }
        return u;
    }

    private List<Map<String, String>> toTagNameList(Set<Integer> tagIds) {
        List<Map<String, String>> list = new ArrayList<>();
        for (Integer id : tagIds.stream().sorted().collect(Collectors.toList())) {
            Tag t = tagMapper.selectById(id);
            Map<String, String> m = new HashMap<>();
            m.put("tagName", t != null && t.getName() != null ? t.getName() : ("#" + id));
            list.add(m);
        }
        return list;
    }
}
