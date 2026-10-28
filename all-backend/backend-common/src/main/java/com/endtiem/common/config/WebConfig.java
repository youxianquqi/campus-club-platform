/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path fileUploadRoot;

    public WebConfig(@Qualifier("fileUploadRoot") Path fileUploadRoot) {
        this.fileUploadRoot = fileUploadRoot;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadLocation = ensureTrailingSlash(fileUploadRoot.toUri().toString());

        registry.addResourceHandler("/activity-images/**")
                .addResourceLocations(uploadLocation + "activity-images/");

        registry.addResourceHandler("/club-logos/**")
                .addResourceLocations(uploadLocation + "club-logos/");

        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(uploadLocation + "avatars/");
    }

    private static String ensureTrailingSlash(String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
