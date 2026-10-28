/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.platform.service.ClubHiddenTagService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club-hidden-tags")
public class ClubHiddenTagController {

    private final ClubHiddenTagService clubHiddenTagService;

    public ClubHiddenTagController(ClubHiddenTagService clubHiddenTagService) {
        this.clubHiddenTagService = clubHiddenTagService;
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<Map<String, Object>>> getHiddenTags(@PathVariable Integer clubId) {
        return ResponseEntity.ok(clubHiddenTagService.getHiddenTags(clubId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addHiddenTag(@RequestBody Map<String, Object> request) {
        try {
            Integer clubId = (Integer) request.get("clubId");
            String tagName = (String) request.get("tagName");
            Object weightObj = request.get("weight");
            String description = (String) request.get("description");

            if (clubId == null || tagName == null || tagName.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "社团 ID 和标签名称不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            BigDecimal weight = null;
            if (weightObj != null) {
                if (weightObj instanceof Number) {
                    weight = BigDecimal.valueOf(((Number) weightObj).doubleValue());
                } else {
                    weight = new BigDecimal(weightObj.toString());
                }
            }

            boolean success =
                    clubHiddenTagService.addHiddenTagByName(
                            clubId, tagName.trim(), weight, description);

            if (success) {
                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "隐藏标签添加成功");
                return ResponseEntity.ok(successResponse);
            }
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "隐藏标签添加失败");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "添加失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeHiddenTag(@RequestBody Map<String, Object> request) {
        try {
            Integer clubId = (Integer) request.get("clubId");
            String tagName = (String) request.get("tagName");

            if (clubId == null || tagName == null || tagName.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "社团 ID 和标签名称不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean success = clubHiddenTagService.removeHiddenTagByName(clubId, tagName.trim());

            if (success) {
                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "隐藏标签删除成功");
                return ResponseEntity.ok(successResponse);
            }
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "隐藏标签不存在或删除失败");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/visible/club/{clubId}")
    public ResponseEntity<List<Map<String, Object>>> getVisibleTags(@PathVariable Integer clubId) {
        return ResponseEntity.ok(clubHiddenTagService.getVisibleTags(clubId));
    }

    @GetMapping("/all/club/{clubId}")
    public ResponseEntity<List<Integer>> getAllTagIdsForMatching(@PathVariable Integer clubId) {
        return ResponseEntity.ok(clubHiddenTagService.getAllTagIdsForMatching(clubId));
    }
}
