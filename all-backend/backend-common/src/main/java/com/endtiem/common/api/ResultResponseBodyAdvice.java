/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.api;

import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** 将非 {@link Result} 的成功响应体包装为统一结构；已含 {@code code}+{@code data} 的 Map（历史接口）不再包一层。 */
@RestControllerAdvice(basePackages = "com.endtiem")
public class ResultResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> t = returnType.getParameterType();
        if (t == Result.class || t == String.class || t == byte[].class) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {
        if (body instanceof Result) {
            return body;
        }
        if (body instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) body;
            if (map.containsKey("code") && map.containsKey("data")) {
                return body;
            }
        }
        return Result.ok(body);
    }
}
