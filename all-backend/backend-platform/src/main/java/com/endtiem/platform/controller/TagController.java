/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.endtiem.platform.entity.Tag;
import com.endtiem.platform.mapper.TagMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagMapper tagMapper;

    public TagController(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    /** 与前端统一：{ code, data }，data 为标签列表（供分类下拉、兴趣标签等）。 */
    @GetMapping
    public Map<String, Object> getAllTags() {
        List<Tag> list = tagMapper.selectList(null);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", list);
        return body;
    }
}
