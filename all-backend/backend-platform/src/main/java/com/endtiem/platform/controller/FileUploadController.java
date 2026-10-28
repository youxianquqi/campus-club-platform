/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final Path fileUploadRoot;

    public FileUploadController(@Qualifier("fileUploadRoot") Path fileUploadRoot) {
        this.fileUploadRoot = fileUploadRoot;
    }

    @PostMapping("/activity-image")
    public ResponseEntity<Map<String, Object>> uploadActivityImage(
            @RequestParam("file") MultipartFile file) {
        return saveUnderSubdir(file, "activity-images/", "/activity-images/");
    }

    @PostMapping("/club-logo")
    public ResponseEntity<Map<String, Object>> uploadClubLogo(
            @RequestParam("file") MultipartFile file) {
        return saveUnderSubdir(file, "club-logos/", "/club-logos/");
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        return saveUnderSubdir(file, "avatars/", "/avatars/");
    }

    private ResponseEntity<Map<String, Object>> saveUnderSubdir(
            MultipartFile file, String subDir, String urlPrefix) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择要上传的文件");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID() + extension;

            Path uploadDir = fileUploadRoot.resolve(subDir).normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String imageUrl = urlPrefix + newFilename;

            result.put("success", true);
            result.put("url", imageUrl);
            result.put("filename", newFilename);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
