/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.endtiem.club.entity.Club;
import com.endtiem.club.entity.ClubTag;
import java.util.List;

/**
 * 社团服务接口 提供社团的增删改查和标签管理功能
 *
 * @since 2026-03-27
 */
public interface ClubService extends IService<Club> {

    /**
     * 根据ID获取社团信息
     *
     * @param id 社团ID
     * @return 社团信息，不存在返回null
     */
    Club getById(Long id);

    /**
     * 获取所有社团列表
     *
     * @return 社团列表
     */
    List<Club> listAll();

    /**
     * 创建新社团
     *
     * @param club 社团信息
     * @return 创建成功返回true
     * @throws IllegalArgumentException 参数为空时抛出
     */
    boolean create(Club club);

    /**
     * 更新社团信息
     *
     * @param club 社团信息（包含ID）
     * @return 更新成功返回true
     * @throws IllegalArgumentException 参数或ID为空时抛出
     * @throws IllegalStateException 社团不存在时抛出
     */
    boolean update(Club club);

    /**
     * 删除社团
     *
     * @param id 社团ID
     * @return 删除成功返回true
     * @throws IllegalArgumentException ID为空时抛出
     * @throws IllegalStateException 社团不存在时抛出
     */
    boolean deleteById(Long id);

    /**
     * 获取社团的标签列表
     *
     * @param clubId 社团ID
     * @return 标签列表
     * @throws IllegalArgumentException ID为空时抛出
     */
    List<ClubTag> getTagsByClubId(Long clubId);

    /**
     * 为社团添加标签
     *
     * @param clubId 社团ID
     * @param tagId 标签ID
     * @return 添加成功返回true
     * @throws IllegalArgumentException ID为空时抛出
     */
    boolean addTag(Long clubId, Long tagId);

    /**
     * 移除社团的标签
     *
     * @param clubId 社团ID
     * @param tagId 标签ID
     * @return 移除成功返回true
     * @throws IllegalArgumentException ID为空时抛出
     */
    boolean removeTag(Long clubId, Long tagId);
}
