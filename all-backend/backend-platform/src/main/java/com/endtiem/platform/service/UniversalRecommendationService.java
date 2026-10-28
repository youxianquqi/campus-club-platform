/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service;

import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubActivity;
import com.endtiem.club.entity.Questionnaire;
import com.endtiem.club.entity.Vote;
import com.endtiem.club.mapper.ClubActivityMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.club.mapper.QuestionnaireMapper;
import com.endtiem.club.mapper.VoteMapper;
import com.endtiem.platform.dto.ActivityRecommendation;
import com.endtiem.platform.dto.QuestionnaireRecommendation;
import com.endtiem.platform.dto.RecruitmentRecommendation;
import com.endtiem.platform.dto.UniversalRecommendation;
import com.endtiem.platform.dto.VoteRecommendation;
import com.endtiem.platform.mapper.TagMapper;
import com.endtiem.platform.service.provider.ActivityTagProvider;
import com.endtiem.platform.service.provider.ClubTagProvider;
import com.endtiem.platform.service.provider.QuestionnaireTagProvider;
import com.endtiem.platform.service.provider.RecruitmentTagProvider;
import com.endtiem.platform.service.provider.VoteTagProvider;
import com.endtiem.recruitment.entity.RecruitmentPost;
import com.endtiem.recruitment.entity.UserTag;
import com.endtiem.recruitment.mapper.RecruitmentPostMapper;
import com.endtiem.recruitment.mapper.UserTagMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UniversalRecommendationService {

    private final TagMatchingService tagMatchingService;
    private final ClubTagProvider clubTagProvider;
    private final ActivityTagProvider activityTagProvider;
    private final VoteTagProvider voteTagProvider;
    private final RecruitmentTagProvider recruitmentTagProvider;
    private final QuestionnaireTagProvider questionnaireTagProvider;
    private final TagMapper tagMapper;
    private final ClubMapper clubMapper;
    private final ClubActivityMapper clubActivityMapper;
    private final VoteMapper voteMapper;
    private final RecruitmentPostMapper recruitmentPostMapper;
    private final QuestionnaireMapper questionnaireMapper;
    private final UserTagMapper userTagMapper;

    public UniversalRecommendationService(
            TagMatchingService tagMatchingService,
            ClubTagProvider clubTagProvider,
            ActivityTagProvider activityTagProvider,
            VoteTagProvider voteTagProvider,
            RecruitmentTagProvider recruitmentTagProvider,
            QuestionnaireTagProvider questionnaireTagProvider,
            TagMapper tagMapper,
            ClubMapper clubMapper,
            ClubActivityMapper clubActivityMapper,
            VoteMapper voteMapper,
            RecruitmentPostMapper recruitmentPostMapper,
            QuestionnaireMapper questionnaireMapper,
            UserTagMapper userTagMapper) {
        this.tagMatchingService = tagMatchingService;
        this.clubTagProvider = clubTagProvider;
        this.activityTagProvider = activityTagProvider;
        this.voteTagProvider = voteTagProvider;
        this.recruitmentTagProvider = recruitmentTagProvider;
        this.questionnaireTagProvider = questionnaireTagProvider;
        this.tagMapper = tagMapper;
        this.clubMapper = clubMapper;
        this.clubActivityMapper = clubActivityMapper;
        this.voteMapper = voteMapper;
        this.recruitmentPostMapper = recruitmentPostMapper;
        this.questionnaireMapper = questionnaireMapper;
        this.userTagMapper = userTagMapper;
    }

    public List<ActivityRecommendation> recommendActivities(int userId, int limit) {
        Set<Integer> userTagIds = getUserTagIds(userId);
        if (userTagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ClubActivity> activities = clubActivityMapper.selectByStatusOrderByCreatedAtDesc(1);
        Map<Integer, Club> clubMap = buildClubMap();

        return activities.stream()
                .map(activity -> convertToActivityRecommendation(activity, userTagIds, clubMap))
                .filter(rec -> rec.getSimilarity() > 0)
                .sorted(
                        Comparator.comparingDouble(ActivityRecommendation::getSimilarity)
                                .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<VoteRecommendation> recommendVotes(int userId, int limit) {
        Set<Integer> userTagIds = getUserTagIds(userId);
        if (userTagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Vote> votes = voteMapper.selectByStatus(1);
        Map<Integer, Club> clubMap = buildClubMap();

        return votes.stream()
                .map(vote -> convertToVoteRecommendation(vote, userTagIds, clubMap))
                .filter(rec -> rec.getSimilarity() > 0)
                .sorted(Comparator.comparingDouble(VoteRecommendation::getSimilarity).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<RecruitmentRecommendation> recommendRecruitments(int userId, int limit) {
        Set<Integer> userTagIds = getUserTagIds(userId);
        if (userTagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<RecruitmentPost> posts = recruitmentPostMapper.selectActivePosts();
        Map<Integer, Club> clubMap = buildClubMap();

        return posts.stream()
                .map(post -> convertToRecruitmentRecommendation(post, userTagIds, clubMap))
                .filter(rec -> rec.getSimilarity() > 0)
                .sorted(
                        Comparator.comparingDouble(RecruitmentRecommendation::getSimilarity)
                                .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<QuestionnaireRecommendation> recommendQuestionnaires(int userId, int limit) {
        Set<Integer> userTagIds = getUserTagIds(userId);
        if (userTagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Questionnaire> questionnaires = questionnaireMapper.selectActive();
        Map<Integer, Club> clubMap = buildClubMap();

        return questionnaires.stream()
                .map(q -> convertToQuestionnaireRecommendation(q, userTagIds, clubMap))
                .filter(rec -> rec.getSimilarity() > 0)
                .sorted(
                        Comparator.comparingDouble(QuestionnaireRecommendation::getSimilarity)
                                .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<UniversalRecommendation> recommendAll(int userId, int limit) {
        Set<Integer> userTagIds = getUserTagIds(userId);
        if (userTagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<UniversalRecommendation> results = new ArrayList<>();

        Map<Integer, Club> clubMap = buildClubMap();

        for (ClubActivity activity : clubActivityMapper.selectByStatusOrderByCreatedAtDesc(1)) {
            double similarity =
                    tagMatchingService.calculateJaccardSimilarity(
                            userTagIds, activityTagProvider.getTagIds(activity));
            if (similarity > 0) {
                Club club = clubMap.get(activity.getClubId());
                results.add(
                        convertToUniversalRecommendation(
                                ActivityTagProvider.ENTITY_TYPE,
                                activity.getId(),
                                activity.getTitle(),
                                activity.getContent(),
                                club != null ? club.getName() : "",
                                activityTagProvider.getTagNames(activity),
                                similarity));
            }
        }

        for (Vote vote : voteMapper.selectByStatus(1)) {
            double similarity =
                    tagMatchingService.calculateJaccardSimilarity(
                            userTagIds, voteTagProvider.getTagIds(vote));
            if (similarity > 0) {
                Club club = clubMap.get(vote.getClubId());
                results.add(
                        convertToUniversalRecommendation(
                                VoteTagProvider.ENTITY_TYPE,
                                vote.getId(),
                                vote.getTitle(),
                                vote.getDescription(),
                                club != null ? club.getName() : "",
                                voteTagProvider.getTagNames(vote),
                                similarity));
            }
        }

        for (RecruitmentPost post : recruitmentPostMapper.selectActivePosts()) {
            double similarity =
                    tagMatchingService.calculateJaccardSimilarity(
                            userTagIds, recruitmentTagProvider.getTagIds(post));
            if (similarity > 0) {
                Club club = post.getClubId() != null ? clubMap.get(post.getClubId()) : null;
                results.add(
                        convertToUniversalRecommendation(
                                RecruitmentTagProvider.ENTITY_TYPE,
                                post.getId(),
                                post.getTitle(),
                                post.getContent(),
                                club != null ? club.getName() : "",
                                recruitmentTagProvider.getTagNames(post),
                                similarity));
            }
        }

        for (Questionnaire q : questionnaireMapper.selectActive()) {
            double similarity =
                    tagMatchingService.calculateJaccardSimilarity(
                            userTagIds, questionnaireTagProvider.getTagIds(q));
            if (similarity > 0) {
                Club club = clubMap.get(q.getClubId());
                results.add(
                        convertToUniversalRecommendation(
                                QuestionnaireTagProvider.ENTITY_TYPE,
                                q.getId(),
                                q.getTitle(),
                                q.getDescription(),
                                club != null ? club.getName() : "",
                                questionnaireTagProvider.getTagNames(q),
                                similarity));
            }
        }

        return results.stream()
                .sorted(
                        Comparator.comparingDouble(UniversalRecommendation::getSimilarity)
                                .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Set<Integer> getUserTagIds(int userId) {
        List<UserTag> userTags = userTagMapper.selectByUserId(userId);
        return userTags.stream().map(UserTag::getTagId).collect(Collectors.toSet());
    }

    private Map<Integer, Club> buildClubMap() {
        return clubMapper.selectList(null).stream()
                .collect(Collectors.toMap(Club::getId, club -> club));
    }

    private ActivityRecommendation convertToActivityRecommendation(
            ClubActivity activity, Set<Integer> userTagIds, Map<Integer, Club> clubMap) {
        Set<Integer> activityTagIds = activityTagProvider.getTagIds(activity);
        double similarity =
                tagMatchingService.calculateJaccardSimilarity(userTagIds, activityTagIds);

        ActivityRecommendation rec = new ActivityRecommendation();
        rec.setId(activity.getId());
        rec.setClubId(activity.getClubId());
        rec.setClubName(
                clubMap.get(activity.getClubId()) != null
                        ? clubMap.get(activity.getClubId()).getName()
                        : "");
        rec.setTitle(activity.getTitle());
        rec.setContent(activity.getContent());
        rec.setImageUrl(activity.getImageUrl());
        rec.setActivityTime(activity.getActivityTime());
        rec.setLocation(activity.getLocation());
        rec.setTags(
                activityTagProvider.getTagNames(activity).stream().collect(Collectors.toList()));
        rec.setParticipants(activity.getCurrentParticipants());
        rec.setMaxParticipants(activity.getMaxParticipants());
        rec.setSimilarity(similarity);
        return rec;
    }

    private VoteRecommendation convertToVoteRecommendation(
            Vote vote, Set<Integer> userTagIds, Map<Integer, Club> clubMap) {
        Set<Integer> voteTagIds = voteTagProvider.getTagIds(vote);
        double similarity = tagMatchingService.calculateJaccardSimilarity(userTagIds, voteTagIds);

        VoteRecommendation rec = new VoteRecommendation();
        rec.setId(vote.getId());
        rec.setClubId(vote.getClubId());
        rec.setClubName(
                clubMap.get(vote.getClubId()) != null
                        ? clubMap.get(vote.getClubId()).getName()
                        : "");
        rec.setTitle(vote.getTitle());
        rec.setDescription(vote.getDescription());
        rec.setVoteType(vote.getVoteType());
        rec.setMaxOptions(vote.getMaxOptions());
        rec.setStartTime(vote.getStartTime());
        rec.setEndTime(vote.getEndTime());
        rec.setTags(voteTagProvider.getTagNames(vote).stream().collect(Collectors.toList()));
        rec.setStatus(vote.getStatus());
        rec.setSimilarity(similarity);
        return rec;
    }

    private RecruitmentRecommendation convertToRecruitmentRecommendation(
            RecruitmentPost post, Set<Integer> userTagIds, Map<Integer, Club> clubMap) {
        Set<Integer> postTagIds = recruitmentTagProvider.getTagIds(post);
        double similarity = tagMatchingService.calculateJaccardSimilarity(userTagIds, postTagIds);

        RecruitmentRecommendation rec = new RecruitmentRecommendation();
        rec.setId(post.getId());
        rec.setUserId(post.getUserId());
        rec.setClubId(post.getClubId());
        rec.setClubName(
                post.getClubId() != null && clubMap.get(post.getClubId()) != null
                        ? clubMap.get(post.getClubId()).getName()
                        : "");
        rec.setTitle(post.getTitle());
        rec.setContent(post.getContent());
        rec.setType(post.getType());
        rec.setTypeName(getTypeName(post.getType()));
        rec.setContact(post.getContact());
        rec.setTags(recruitmentTagProvider.getTagNames(post).stream().collect(Collectors.toList()));
        rec.setDeadline(post.getDeadline());
        rec.setViewCount(post.getViewCount());
        rec.setCreatedAt(post.getCreatedAt());
        rec.setSimilarity(similarity);
        return rec;
    }

    private QuestionnaireRecommendation convertToQuestionnaireRecommendation(
            Questionnaire q, Set<Integer> userTagIds, Map<Integer, Club> clubMap) {
        Set<Integer> qTagIds = questionnaireTagProvider.getTagIds(q);
        double similarity = tagMatchingService.calculateJaccardSimilarity(userTagIds, qTagIds);

        QuestionnaireRecommendation rec = new QuestionnaireRecommendation();
        rec.setId(q.getId());
        rec.setClubId(q.getClubId());
        rec.setClubName(
                clubMap.get(q.getClubId()) != null ? clubMap.get(q.getClubId()).getName() : "");
        rec.setTitle(q.getTitle());
        rec.setDescription(q.getDescription());
        rec.setStartTime(q.getStartTime());
        rec.setEndTime(q.getEndTime());
        rec.setTags(questionnaireTagProvider.getTagNames(q).stream().collect(Collectors.toList()));
        rec.setStatus(q.getStatus());
        rec.setAllowAnonymous(q.getAllowAnonymous());
        rec.setSimilarity(similarity);
        return rec;
    }

    private UniversalRecommendation convertToUniversalRecommendation(
            String entityType,
            Integer id,
            String title,
            String description,
            String subTitle,
            Set<String> tags,
            double similarity) {
        UniversalRecommendation rec = new UniversalRecommendation();
        rec.setEntityType(entityType);
        rec.setId(id);
        rec.setTitle(title);
        rec.setDescription(description);
        rec.setSubTitle(subTitle);
        rec.setTags(new ArrayList<>(tags));
        rec.setSimilarity(similarity);
        rec.setTimestamp(System.currentTimeMillis());
        return rec;
    }

    private String getTypeName(Integer type) {
        if (type == null) {
            return "其他";
        }
        switch (type) {
            case 1:
                return "技术岗";
            case 2:
                return "运营岗";
            case 3:
                return "设计岗";
            default:
                return "其他";
        }
    }
}
