/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.entity.KnowledgeArticle;
import com.endtiem.platform.service.KnowledgeArticleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeArticleController {

    private final KnowledgeArticleService knowledgeArticleService;
    private final JwtUtil jwtUtil;

    public KnowledgeArticleController(
            KnowledgeArticleService knowledgeArticleService, JwtUtil jwtUtil) {
        this.knowledgeArticleService = knowledgeArticleService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Map<String, Object> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "time") String sort) {
        IPage<KnowledgeArticle> articles =
                knowledgeArticleService.getPublishedArticles(
                        page, pageSize, category, keyword, sort);

        Map<String, Object> result = new HashMap<>();
        result.put("articles", articles.getRecords());
        result.put("total", articles.getTotal());
        result.put("page", articles.getCurrent());
        result.put("pageSize", articles.getSize());
        result.put("pages", articles.getPages());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getArticleById(@PathVariable Long id) {
        KnowledgeArticle article = knowledgeArticleService.getArticleById(id);
        if (article == null || article.getStatus() == 2) {
            throw new RuntimeException("文章不存在");
        }
        knowledgeArticleService.incrementViewCount(id);
        article = knowledgeArticleService.getArticleById(id);
        return toArticleMap(article);
    }

    @PostMapping
    public Map<String, Object> createArticle(@RequestBody Map<String, Object> request) {
        Long authorId = getCurrentUserId();

        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String coverImage = (String) request.get("coverImage");
        String images = request.get("images") != null ? request.get("images").toString() : null;
        String category = (String) request.get("category");

        if (title == null || title.isEmpty()) {
            throw new RuntimeException("标题不能为空");
        }
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }

        KnowledgeArticle article =
                knowledgeArticleService.createArticle(
                        authorId, title, content, coverImage, images, category);

        return toArticleMap(article);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateArticle(
            @PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long authorId = getCurrentUserId();

        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String coverImage = (String) request.get("coverImage");
        String images = request.get("images") != null ? request.get("images").toString() : null;
        String category = (String) request.get("category");
        Integer status =
                request.get("status") != null ? ((Number) request.get("status")).intValue() : null;

        KnowledgeArticle article =
                knowledgeArticleService.updateArticle(
                        id, authorId, title, content, coverImage, images, category, status);

        return toArticleMap(article);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteArticle(@PathVariable Long id) {
        Long authorId = getCurrentUserId();
        knowledgeArticleService.deleteArticle(id, authorId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "文章已删除");
        return result;
    }

    @GetMapping("/my")
    public Map<String, Object> getMyArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "time") String sort) {
        Long authorId = getCurrentUserId();
        IPage<KnowledgeArticle> articles =
                knowledgeArticleService.getArticlesByAuthor(authorId, page, pageSize, sort);

        Map<String, Object> result = new HashMap<>();
        result.put("articles", articles.getRecords());
        result.put("total", articles.getTotal());
        result.put("page", articles.getCurrent());
        result.put("pageSize", articles.getSize());
        result.put("pages", articles.getPages());
        return result;
    }

    @GetMapping("/categories")
    public Map<String, Object> getCategories() {
        List<String> categories = knowledgeArticleService.getAllCategories();
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        return result;
    }

    @GetMapping("/recent")
    public Map<String, Object> getRecentArticles(@RequestParam(defaultValue = "5") int limit) {
        List<KnowledgeArticle> articles = knowledgeArticleService.getRecentArticles(limit);
        Map<String, Object> result = new HashMap<>();
        result.put("articles", articles);
        return result;
    }

    @GetMapping("/popular")
    public Map<String, Object> getPopularArticles(@RequestParam(defaultValue = "5") int limit) {
        List<KnowledgeArticle> articles = knowledgeArticleService.getPopularArticles(limit);
        Map<String, Object> result = new HashMap<>();
        result.put("articles", articles);
        return result;
    }

    private Long getCurrentUserId() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("请先登录");
        }
        token = token.replace("Bearer ", "");
        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        return userId != null ? userId.longValue() : null;
    }

    private Map<String, Object> toArticleMap(KnowledgeArticle article) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", article.getId());
        item.put("authorId", article.getAuthorId());
        item.put("title", article.getTitle());
        item.put("content", article.getContent());
        item.put("coverImage", article.getCoverImage());
        item.put("images", article.getImages());
        item.put("category", article.getCategory());
        item.put("viewCount", article.getViewCount());
        item.put("likeCount", article.getLikeCount());
        item.put("createdAt", article.getCreatedAt());
        item.put("updatedAt", article.getUpdatedAt());
        item.put("status", article.getStatus());
        return item;
    }
}
