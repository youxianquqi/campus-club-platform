/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.club.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.Data;

@Data
public class ClubRequest {
    private String name;
    private String description;
    private String qqGroup;
    private List<Integer> tags;

    /** 与前端「社团类型」一致，可与 tags 合并写入 club_tag */
    private Integer typeId;

    /** 前端字段名为 logo，入库 logo_url */
    @JsonAlias("logo")
    private String logoUrl;

    /** 创建页扩展信息，拼入 description 持久化 */
    private String activityPlan;

    private String requirements;
}
