/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.platform.service.ClubInterMemberMatchService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 社团间匹配：基于两社团在社成员的兴趣标签（user_tag），成员两两 Jaccard 平均。 */
@RestController
@RequestMapping("/api/clubs")
public class ClubInterMemberMatchController {

    private final ClubInterMemberMatchService matchService;

    public ClubInterMemberMatchController(ClubInterMemberMatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/member-match")
    public Map<String, Object> memberMatch(
            @RequestParam("clubId1") int clubId1, @RequestParam("clubId2") int clubId2) {
        Map<String, Object> data = matchService.match(clubId1, clubId2);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", data);
        return body;
    }
}
