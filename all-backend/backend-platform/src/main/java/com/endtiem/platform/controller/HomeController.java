/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubActivity;
import com.endtiem.club.mapper.ClubActivityMapper;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.recruitment.mapper.ClubMemberMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 首页聚合数据，对齐前端 homeApi.overview / banners。 */
@Slf4j
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final ClubMapper clubMapper;
    private final ClubActivityMapper clubActivityMapper;
    private final ClubMemberMapper clubMemberMapper;

    public HomeController(
            ClubMapper clubMapper,
            ClubActivityMapper clubActivityMapper,
            ClubMemberMapper clubMemberMapper) {
        this.clubMapper = clubMapper;
        this.clubActivityMapper = clubActivityMapper;
        this.clubMemberMapper = clubMemberMapper;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(
            @RequestParam(defaultValue = "8") int hotLimit,
            @RequestParam(defaultValue = "9") int activitySize) {
        int hl = Math.min(Math.max(hotLimit, 1), 30);
        int as = Math.min(Math.max(activitySize, 1), 30);

        List<Map<String, Object>> hotClubs = loadHotClubs(hl);
        List<Map<String, Object>> recentActivities = loadRecentActivities(as);

        Map<String, Object> data = new HashMap<>();
        data.put("banners", defaultBanners());
        data.put("hotClubs", hotClubs);
        data.put("recentActivities", recentActivities);
        data.put("activities", recentActivities);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", data);
        return body;
    }

    private List<Map<String, Object>> loadHotClubs(int limit) {
        List<Map<String, Object>> hotClubs = new ArrayList<>();
        try {
            Page<Club> clubPage = clubMapper.selectPage(new Page<>(1, limit), null);
            for (Club c : clubPage.getRecords()) {
                hotClubs.add(toClubCard(c));
            }
        } catch (Exception e) {
            log.warn("home overview: 加载热门社团失败", e);
        }
        return hotClubs;
    }

    private List<Map<String, Object>> loadRecentActivities(int size) {
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        try {
            Page<ClubActivity> actPage =
                    clubActivityMapper.pageByStatusOrderByCreatedAtDesc(1, new Page<>(1, size));
            for (ClubActivity a : actPage.getRecords()) {
                recentActivities.add(toActivityCard(a));
            }
        } catch (Exception e) {
            log.warn("home overview: 加载近期活动失败", e);
        }
        return recentActivities;
    }

    @GetMapping("/banners")
    public Map<String, Object> banners() {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", defaultBanners());
        return body;
    }

    private List<Map<String, Object>> defaultBanners() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> b1 = new HashMap<>();
        b1.put("image", "/img-placeholder.svg");
        b1.put("title", "发现你的社团");
        b1.put("desc", "加入社团之家，参与精彩活动");
        list.add(b1);
        return list;
    }

    private Map<String, Object> toClubCard(Club c) {
        Map<String, Object> m = new HashMap<>();
        Integer cid = c.getId();
        m.put("id", cid);
        m.put("name", c.getName());
        m.put("description", c.getDescription());
        m.put("logo", c.getLogoUrl());
        m.put("cover", c.getLogoUrl());
        m.put("logoUrl", c.getLogoUrl());
        m.put("adminId", c.getAdminId());
        m.put("leaderId", c.getAdminId());
        m.put("createdAt", c.getCreatedAt());
        m.put("rating", null);
        long mc = 0;
        if (cid != null) {
            try {
                mc = clubMemberMapper.countActiveByClubId(cid.longValue());
            } catch (Exception e) {
                log.debug("home overview: 社团 {} 成员数统计失败", cid, e);
            }
        }
        m.put("memberCount", mc);
        return m;
    }

    private Map<String, Object> toActivityCard(ClubActivity a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("clubId", a.getClubId());
        m.put("title", a.getTitle());
        m.put("description", a.getContent());
        m.put("content", a.getContent());
        m.put("cover", a.getImageUrl());
        m.put("imageUrl", a.getImageUrl());
        m.put("location", a.getLocation());
        /* 审核通过(1) 在前台首页按「可报名」展示，与 Activities 页 status===2 对齐 */
        Integer st = a.getStatus();
        m.put("status", st != null && st == 1 ? 2 : st);
        m.put("startTime", a.getActivityTime());
        m.put("activityTime", a.getActivityTime());
        m.put("createdAt", a.getCreatedAt());
        m.put("maxParticipants", null);
        m.put("currentParticipants", 0);
        return m;
    }
}
