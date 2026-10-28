/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.endtiem")
@MapperScan({
    "com.endtiem.admin.mapper",
    "com.endtiem.recruitment.mapper",
    "com.endtiem.club.mapper",
    "com.endtiem.platform.mapper",
    "com.endtiem.common.mapper"
})
public class EndtiemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EndtiemApplication.class, args);
    }
}
