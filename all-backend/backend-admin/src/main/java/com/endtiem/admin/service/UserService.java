/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.endtiem.admin.entity.User;

public interface UserService extends IService<User> {

    User getById(Integer id);

    User getByUsername(String username);

    User createUser(String username, String password, Integer role);

    boolean updatePassword(Integer userId, String oldPassword, String newPassword);

    boolean updateRole(Integer userId, Integer role);
}
