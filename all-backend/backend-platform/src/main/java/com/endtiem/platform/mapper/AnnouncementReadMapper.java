/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.AnnouncementRead;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {

    default Optional<AnnouncementRead> selectByAnnouncementIdAndStudentId(
            Integer announcementId, Integer studentId) {
        return Optional.ofNullable(
                selectOne(
                        new LambdaQueryWrapper<AnnouncementRead>()
                                .eq(AnnouncementRead::getAnnouncementId, announcementId)
                                .eq(AnnouncementRead::getStudentId, studentId)));
    }

    default List<AnnouncementRead> selectByStudentId(Integer studentId) {
        return selectList(
                new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getStudentId, studentId));
    }

    default boolean existsByAnnouncementIdAndStudentId(Integer announcementId, Integer studentId) {
        return selectCount(
                        new LambdaQueryWrapper<AnnouncementRead>()
                                .eq(AnnouncementRead::getAnnouncementId, announcementId)
                                .eq(AnnouncementRead::getStudentId, studentId))
                > 0;
    }

    default long countByAnnouncementId(Integer announcementId) {
        return selectCount(
                new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getAnnouncementId, announcementId));
    }

    default int deleteByAnnouncementId(Integer announcementId) {
        return delete(
                new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getAnnouncementId, announcementId));
    }
}
