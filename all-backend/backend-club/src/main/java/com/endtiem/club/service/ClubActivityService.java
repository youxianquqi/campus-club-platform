/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.endtiem.club.entity.ClubActivity;
import java.util.List;

public interface ClubActivityService extends IService<ClubActivity> {

    Page<ClubActivity> pageByStatus(Integer status, int pageNum, int pageSize);

    ClubActivity getById(Integer id);

    List<ClubActivity> listByClubId(Integer clubId);

    List<ClubActivity> listPendingActivities();

    ClubActivity create(ClubActivity activity);

    ClubActivity update(ClubActivity activity);

    ClubActivity approve(Integer id);

    ClubActivity reject(Integer id, String reason);

    boolean deleteById(Integer id);
}
