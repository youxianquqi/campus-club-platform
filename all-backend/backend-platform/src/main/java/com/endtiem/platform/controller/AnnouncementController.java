/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.entity.Announcement;
import com.endtiem.platform.entity.AnnouncementRead;
import com.endtiem.platform.mapper.AnnouncementMapper;
import com.endtiem.platform.mapper.AnnouncementReadMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementReadMapper announcementReadMapper;
    private final JwtUtil jwtUtil;

    public AnnouncementController(
            AnnouncementMapper announcementMapper,
            AnnouncementReadMapper announcementReadMapper,
            JwtUtil jwtUtil) {
        this.announcementMapper = announcementMapper;
        this.announcementReadMapper = announcementReadMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Map<String, Object>> getAllAnnouncements() {
        List<Announcement> announcements = announcementMapper.selectAllOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Announcement ann : announcements) {
            result.add(toMap(ann));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getAnnouncementById(@PathVariable Integer id) {
        Announcement ann = announcementMapper.selectById(id);
        if (ann == null) {
            throw new RuntimeException("公告不存在");
        }
        return toMap(ann);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer studentId = jwtUtil.getRelatedIdFromToken(token);

        List<Announcement> allAnnouncements = announcementMapper.selectAllOrderByCreatedAtDesc();
        int unreadCount = 0;

        for (Announcement ann : allAnnouncements) {
            if (!announcementReadMapper.existsByAnnouncementIdAndStudentId(
                    ann.getId(), studentId)) {
                unreadCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", unreadCount);
        return result;
    }

    @GetMapping("/with-read-status")
    public List<Map<String, Object>> getAnnouncementsWithReadStatus() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer studentId = jwtUtil.getRelatedIdFromToken(token);

        List<Announcement> announcements = announcementMapper.selectAllOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Announcement ann : announcements) {
            Map<String, Object> item = toMap(ann);
            item.put(
                    "isRead",
                    announcementReadMapper.existsByAnnouncementIdAndStudentId(
                            ann.getId(), studentId));
            result.add(item);
        }
        return result;
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer studentId = jwtUtil.getRelatedIdFromToken(token);

        if (!announcementReadMapper.existsByAnnouncementIdAndStudentId(id, studentId)) {
            AnnouncementRead read = new AnnouncementRead();
            read.setAnnouncementId(id);
            read.setStudentId(studentId);
            read.setReadAt(LocalDateTime.now());
            announcementReadMapper.insert(read);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @PostMapping
    public Announcement createAnnouncement(@RequestBody Map<String, String> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role != 1) {
            throw new RuntimeException("只有系统管理员可以创建公告");
        }

        LocalDateTime now = LocalDateTime.now();
        Announcement announcement = new Announcement();
        announcement.setTitle(request.get("title"));
        announcement.setContent(request.get("content"));
        announcement.setCreatedAt(now);
        announcement.setUpdatedAt(now);
        announcementMapper.insert(announcement);
        return announcement;
    }

    @PutMapping("/{id}")
    public Announcement updateAnnouncement(
            @PathVariable Integer id, @RequestBody Map<String, String> request) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role != 1) {
            throw new RuntimeException("只有系统管理员可以编辑公告");
        }

        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        if (request.get("title") != null) {
            announcement.setTitle(request.get("title"));
        }
        if (request.get("content") != null) {
            announcement.setContent(request.get("content"));
        }
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        return announcement;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteAnnouncement(@PathVariable Integer id) {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role != 1) {
            throw new RuntimeException("只有系统管理员可以删除公告");
        }

        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        announcementReadMapper.deleteByAnnouncementId(id);
        announcementMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "公告已删除");
        return result;
    }

    private static Map<String, Object> toMap(Announcement ann) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", ann.getId());
        item.put("title", ann.getTitle());
        item.put("content", ann.getContent());
        item.put("createdAt", ann.getCreatedAt());
        item.put("updatedAt", ann.getUpdatedAt());
        return item;
    }
}
