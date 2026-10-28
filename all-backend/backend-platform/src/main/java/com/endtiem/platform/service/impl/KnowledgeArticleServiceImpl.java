/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.platform.entity.KnowledgeArticle;
import com.endtiem.platform.mapper.KnowledgeArticleMapper;
import com.endtiem.platform.service.KnowledgeArticleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {

    private final KnowledgeArticleMapper knowledgeArticleMapper;

    public KnowledgeArticleServiceImpl(KnowledgeArticleMapper knowledgeArticleMapper) {
        this.knowledgeArticleMapper = knowledgeArticleMapper;
    }

    @Override
    public IPage<KnowledgeArticle> getPublishedArticles(
            int page, int pageSize, String category, String keyword, String sort) {
        Page<KnowledgeArticle> pageParam = new Page<>(page, pageSize);
        boolean ascending = sort != null && sort.startsWith("asc");
        String sortField = sort;
        if (sort != null && sort.startsWith("asc:")) {
            sortField = sort.substring(5);
        } else if (sort != null && sort.startsWith("desc:")) {
            sortField = sort.substring(6);
        }
        return knowledgeArticleMapper.selectPublishedArticles(
                pageParam, category, keyword, sortField, ascending);
    }

    @Override
    public KnowledgeArticle getArticleById(Long id) {
        return knowledgeArticleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            knowledgeArticleMapper.updateById(article);
        }
    }

    @Override
    @Transactional
    public KnowledgeArticle createArticle(
            Long authorId,
            String title,
            String content,
            String coverImage,
            String images,
            String category) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setAuthorId(authorId);
        article.setTitle(title);
        article.setContent(content);
        article.setCoverImage(coverImage);
        article.setImages(images);
        article.setCategory(category);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setStatus(1);
        knowledgeArticleMapper.insert(article);
        return article;
    }

    @Override
    @Transactional
    public KnowledgeArticle updateArticle(
            Long id,
            Long authorId,
            String title,
            String content,
            String coverImage,
            String images,
            String category,
            Integer status) {
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限修改此文章");
        }

        if (title != null) {
            article.setTitle(title);
        }
        if (content != null) {
            article.setContent(content);
        }
        if (coverImage != null) {
            article.setCoverImage(coverImage);
        }
        if (images != null) {
            article.setImages(images);
        }
        if (category != null) {
            article.setCategory(category);
        }
        if (status != null) {
            article.setStatus(status);
        }
        article.setUpdatedAt(LocalDateTime.now());
        knowledgeArticleMapper.updateById(article);
        return article;
    }

    @Override
    @Transactional
    public void deleteArticle(Long id, Long authorId) {
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限删除此文章");
        }
        article.setStatus(2);
        article.setUpdatedAt(LocalDateTime.now());
        knowledgeArticleMapper.updateById(article);
    }

    @Override
    public IPage<KnowledgeArticle> getArticlesByAuthor(
            Long authorId, int page, int pageSize, String sort) {
        Page<KnowledgeArticle> pageParam = new Page<>(page, pageSize);
        boolean ascending = sort != null && sort.startsWith("asc");
        String sortField = sort;
        if (sort != null && sort.startsWith("asc:")) {
            sortField = sort.substring(5);
        } else if (sort != null && sort.startsWith("desc:")) {
            sortField = sort.substring(6);
        }
        return knowledgeArticleMapper.selectByAuthorId(pageParam, authorId, sortField, ascending);
    }

    @Override
    public List<String> getAllCategories() {
        List<KnowledgeArticle> articles =
                knowledgeArticleMapper.selectList(
                        new LambdaQueryWrapper<KnowledgeArticle>()
                                .eq(KnowledgeArticle::getStatus, 1)
                                .isNotNull(KnowledgeArticle::getCategory)
                                .select(KnowledgeArticle::getCategory)
                                .groupBy(KnowledgeArticle::getCategory));
        return articles.stream()
                .map(KnowledgeArticle::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeArticle> getRecentArticles(int limit) {
        return knowledgeArticleMapper.selectRecentPublished(limit);
    }

    @Override
    public List<KnowledgeArticle> getPopularArticles(int limit) {
        return knowledgeArticleMapper.selectPopularPublished(limit);
    }
}
