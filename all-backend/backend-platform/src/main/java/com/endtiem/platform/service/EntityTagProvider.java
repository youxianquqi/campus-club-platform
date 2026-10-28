/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.service;

import java.util.Set;

/**
 * 实体标签提供者接口 用于从不同类型的实体中获取标签集合 支持两种标签存储格式： 1. 关联表形式（如 club_tag） 2. 逗号分隔字符串形式（如 activity.tags）
 *
 * @param <T> 实体类型
 */
public interface EntityTagProvider<T> {

    /**
     * 获取实体的标签ID集合
     *
     * @param entity 实体对象
     * @return 标签ID集合
     */
    Set<Integer> getTagIds(T entity);

    /**
     * 获取实体的标签名称集合
     *
     * @param entity 实体对象
     * @return 标签名称集合
     */
    Set<String> getTagNames(T entity);

    /**
     * 获取标签提供者对应的实体类型
     *
     * @return 实体类型标识
     */
    String getEntityType();

    /**
     * 检查实体是否有效（是否有标签）
     *
     * @param entity 实体对象
     * @return true 如果实体有效
     */
    default boolean isValid(T entity) {
        Set<Integer> tagIds = getTagIds(entity);
        return tagIds != null && !tagIds.isEmpty();
    }
}
