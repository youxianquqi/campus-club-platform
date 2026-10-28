/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.endtiem.admin.entity.User;

public interface UserService extends IService<User> {

    User getById(Integer id);

    /**
     * @deprecated student 已合并至 user，请使用 {@link #getById(Integer)}（参数为 user.id）
     */
    @Deprecated
    User getByRelatedId(Integer relatedId);

    User getCurrentUser(String token);

    User updateInfo(User user);

    User updateAvatar(String token, String avatar);

    boolean setRecommendBoost(String token, Integer clubId);
}
