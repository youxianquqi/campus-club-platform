/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.recruitment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.endtiem.recruitment.entity.ClubMember;
import java.util.List;
import java.util.Map;

public interface ClubMemberService extends IService<ClubMember> {

    List<Map<String, Object>> getMembersByClubId(Long clubId);

    ClubMember joinClub(String token, Long clubId);

    boolean removeMember(String token, Long memberId);

    boolean removeMemberByClubAndUser(String token, Long clubId, Long userId);

    boolean updateMemberRole(String token, Long memberId, Integer newRole);
}
