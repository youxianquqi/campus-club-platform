/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 与 item 项目一致：{@code file.upload.path} 可为绝对路径（如 {@code f:/enditeam/backend/uploads/}），
 * 也可为相对路径；此处统一解析为绝对路径，供上传与静态资源映射共用。
 */
@Configuration
public class FileUploadRootConfig {

    @Bean(name = "fileUploadRoot")
    public Path fileUploadRoot(@Value("${file.upload.path:uploads/}") String rawPath) {
        return Paths.get(rawPath).toAbsolutePath().normalize();
    }
}
