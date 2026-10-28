/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.admin.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.admin.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    default User selectByUsername(String username) {
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    /** 兼容历史语义：relatedId 现与 {@code user.id} 一致（student 表已并入 user）。 */
    default User selectByRelatedId(Integer relatedId) {
        if (relatedId == null) {
            return null;
        }
        return selectById(relatedId);
    }

    default User selectByStudentId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getStudentId, studentId.trim()));
    }

    default User selectByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone.trim()));
    }

    default User selectByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email.trim()));
    }

    @Select(
            "(SELECT * FROM `user` WHERE CAST(student_id AS SIGNED) < (SELECT CAST(student_id AS"
                + " SIGNED) FROM `user` WHERE id = #{currentId}) ORDER BY CAST(student_id AS"
                + " SIGNED) DESC LIMIT 1) UNION ALL (SELECT * FROM `user` WHERE CAST(student_id AS"
                + " SIGNED) > (SELECT CAST(student_id AS SIGNED) FROM `user` WHERE id ="
                + " #{currentId}) ORDER BY CAST(student_id AS SIGNED) ASC LIMIT 1)")
    List<User> selectNeighboringStudents(@Param("currentId") Integer currentId);
}
