/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.aop;

import com.endtiem.common.dto.PasswordChangeRequest;
import com.endtiem.common.dto.RegisterRequest;
import com.endtiem.common.dto.StudentLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/** 统一记录所有 Controller 公共方法的入口参数（脱敏/截断）、HTTP 方法与 URI、耗时与结果状态。 */
@Aspect
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ControllerRequestLoggingAspect {

    private static final int MAX_PARAM_LEN = 2048;

    private final ObjectMapper objectMapper;

    @Pointcut("execution(public * com.endtiem..controller..*.*(..))")
    public void controllerPublicMethods() {}

    @Around("controllerPublicMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String sig = pjp.getSignature().toShortString();
        String http = currentHttpDescriptor();
        String params = formatArgs(pjp.getArgs());
        log.info("→ {} {} | params: {}", http, sig, params);
        long t0 = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long ms = (System.nanoTime() - t0) / 1_000_000L;
            log.info("← {} {} | {} ms | OK", http, sig, ms);
            return result;
        } catch (Throwable ex) {
            long ms = (System.nanoTime() - t0) / 1_000_000L;
            log.warn("← {} {} | {} ms | FAIL: {}", http, sig, ms, ex.getMessage());
            throw ex;
        }
    }

    private String currentHttpDescriptor() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return "-";
            }
            return attrs.getRequest().getMethod() + " " + attrs.getRequest().getRequestURI();
        } catch (Exception e) {
            return "-";
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        List<String> parts = new ArrayList<>();
        for (Object arg : args) {
            parts.add(formatOne(arg));
        }
        return "[" + String.join(", ", parts) + "]";
    }

    private String formatOne(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
            return arg.getClass().getSimpleName();
        }
        if (arg instanceof MultipartFile f) {
            return String.format(
                    "MultipartFile(name=%s,size=%d)", f.getOriginalFilename(), f.getSize());
        }
        if (arg instanceof MultipartFile[] arr) {
            StringBuilder sb = new StringBuilder("MultipartFile[](");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                if (arr[i] == null) {
                    sb.append("null");
                } else {
                    sb.append(arr[i].getOriginalFilename()).append(":").append(arr[i].getSize());
                }
            }
            sb.append(")");
            return sb.toString();
        }
        if (arg instanceof Collection<?> c) {
            if (!c.isEmpty() && c.iterator().next() instanceof MultipartFile) {
                List<String> bits = new ArrayList<>();
                for (Object o : c) {
                    if (o instanceof MultipartFile f) {
                        bits.add(f.getOriginalFilename() + ":" + f.getSize());
                    }
                }
                return "MultipartFiles(" + String.join(",", bits) + ")";
            }
        }
        if (arg instanceof StudentLoginRequest r) {
            return String.format("StudentLoginRequest(username=%s,password=***)", r.getUsername());
        }
        if (arg instanceof RegisterRequest r) {
            return String.format(
                    "RegisterRequest(username=%s,email=%s,phone=%s,password=***)",
                    r.getUsername(), r.getEmail(), r.getPhone());
        }
        if (arg instanceof PasswordChangeRequest) {
            return "PasswordChangeRequest(oldPassword=***,newPassword=***)";
        }
        if (arg instanceof Map) {
            try {
                String s = objectMapper.writeValueAsString(sanitizeMap((Map<?, ?>) arg));
                if (s.length() > MAX_PARAM_LEN) {
                    return s.substring(0, MAX_PARAM_LEN) + "...(truncated)";
                }
                return s;
            } catch (Exception ex) {
                return String.valueOf(arg);
            }
        }
        if (arg instanceof byte[]) {
            return "byte[len=" + ((byte[]) arg).length + "]";
        }
        try {
            String s = objectMapper.writeValueAsString(arg);
            if (s.length() > MAX_PARAM_LEN) {
                return s.substring(0, MAX_PARAM_LEN) + "...(truncated)";
            }
            return s;
        } catch (Exception e) {
            return arg.getClass().getSimpleName()
                    + "@"
                    + Integer.toHexString(System.identityHashCode(arg));
        }
    }

    private Map<String, Object> sanitizeMap(Map<?, ?> map) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String key = String.valueOf(e.getKey());
            Object v = e.getValue();
            if (key.toLowerCase().contains("password")) {
                copy.put(key, "***");
            } else if (v instanceof Map) {
                copy.put(key, sanitizeMap((Map<?, ?>) v));
            } else {
                copy.put(key, v);
            }
        }
        return copy;
    }
}
