/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.platform.entity.KnowledgeArticle;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeArticleMapper extends BaseMapper<KnowledgeArticle> {

    default IPage<KnowledgeArticle> selectPublishedArticles(
            Page<KnowledgeArticle> page,
            String category,
            String keyword,
            String sortField,
            boolean ascending) {
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeArticle::getStatus, 1);

        if (category != null && !category.isEmpty()) {
            wrapper.eq(KnowledgeArticle::getCategory, category);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(
                    w ->
                            w.like(KnowledgeArticle::getTitle, keyword)
                                    .or()
                                    .like(KnowledgeArticle::getContent, keyword));
        }

        if ("viewCount".equals(sortField)) {
            if (ascending) {
                wrapper.orderByAsc(KnowledgeArticle::getViewCount);
            } else {
                wrapper.orderByDesc(KnowledgeArticle::getViewCount);
            }
        } else {
            wrapper.orderByDesc(KnowledgeArticle::getCreatedAt);
        }

        return selectPage(page, wrapper);
    }

    default IPage<KnowledgeArticle> selectByAuthorId(
            Page<KnowledgeArticle> page, Long authorId, String sortField, boolean ascending) {
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeArticle::getAuthorId, authorId);
        wrapper.ne(KnowledgeArticle::getStatus, 2);

        if ("viewCount".equals(sortField)) {
            if (ascending) {
                wrapper.orderByAsc(KnowledgeArticle::getViewCount);
            } else {
                wrapper.orderByDesc(KnowledgeArticle::getViewCount);
            }
        } else {
            wrapper.orderByDesc(KnowledgeArticle::getCreatedAt);
        }

        return selectPage(page, wrapper);
    }

    default List<KnowledgeArticle> selectRecentPublished(int limit) {
        return selectList(
                new LambdaQueryWrapper<KnowledgeArticle>()
                        .eq(KnowledgeArticle::getStatus, 1)
                        .orderByDesc(KnowledgeArticle::getCreatedAt)
                        .last("LIMIT " + limit));
    }

    default List<KnowledgeArticle> selectPopularPublished(int limit) {
        return selectList(
                new LambdaQueryWrapper<KnowledgeArticle>()
                        .eq(KnowledgeArticle::getStatus, 1)
                        .orderByDesc(KnowledgeArticle::getViewCount)
                        .last("LIMIT " + limit));
    }
}
