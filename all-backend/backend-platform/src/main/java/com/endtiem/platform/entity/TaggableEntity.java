/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.entity;

/** 可标签化实体接口 所有需要支持标签匹配的实体都应实现此接口 */
public interface TaggableEntity {

    /** 获取实体唯一标识 */
    Integer getId();

    /** 获取标签字符串（逗号分隔格式） 如: "编程,音乐,篮球" */
    String getTags();

    /** 获取实体类型标识 */
    String getEntityType();
}
