/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.endtiem.platform.entity.KnowledgeArticle;
import java.util.List;

public interface KnowledgeArticleService {

    IPage<KnowledgeArticle> getPublishedArticles(
            int page, int pageSize, String category, String keyword, String sort);

    KnowledgeArticle getArticleById(Long id);

    void incrementViewCount(Long id);

    KnowledgeArticle createArticle(
            Long authorId,
            String title,
            String content,
            String coverImage,
            String images,
            String category);

    KnowledgeArticle updateArticle(
            Long id,
            Long authorId,
            String title,
            String content,
            String coverImage,
            String images,
            String category,
            Integer status);

    void deleteArticle(Long id, Long authorId);

    IPage<KnowledgeArticle> getArticlesByAuthor(Long authorId, int page, int pageSize, String sort);

    List<String> getAllCategories();

    List<KnowledgeArticle> getRecentArticles(int limit);

    List<KnowledgeArticle> getPopularArticles(int limit);
}
