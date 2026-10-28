/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** JWT 工具（jjwt 0.12.x，兼容 Spring Boot 3 / Java 17）。密钥长度不足 32 字节时用 SHA-256 派生。 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:endtiem-dev-change-me-in-production}")
    private String secret;

    @Value("${jwt.expire-ms:3600000}")
    private long expireTime;

    private SecretKey signingKey() {
        byte[] buf = secret.getBytes(StandardCharsets.UTF_8);
        if (buf.length < 32) {
            try {
                buf = MessageDigest.getInstance("SHA-256").digest(buf);
            } catch (Exception e) {
                throw new IllegalStateException("JWT secret derivation failed", e);
            }
        } else {
            buf = Arrays.copyOf(buf, 32);
        }
        return Keys.hmacShaKeyFor(buf);
    }

    /**
     * @param relatedId 业务主体 ID，学生场景与 {@code user.id} 一致（原 student 已合并至 user）
     * @param accountUserId 登录账号主键 {@code user.id}，写入招募帖等需绑定账号表时使用
     */
    public String generateToken(
            String username, Integer role, Integer relatedId, Integer accountUserId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        claims.put("relatedId", relatedId);
        if (accountUserId != null) {
            claims.put("accountUserId", accountUserId);
        }
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey())
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).get("username", String.class);
    }

    public Integer getRoleFromToken(String token) {
        return parseToken(token).get("role", Integer.class);
    }

    public Integer getRelatedIdFromToken(String token) {
        return parseToken(token).get("relatedId", Integer.class);
    }

    /** 登录表 {@code user.id}；旧令牌可能无此字段 */
    public Integer getAccountUserIdFromToken(String token) {
        return parseToken(token).get("accountUserId", Integer.class);
    }

    /** 与招募侧 user_tag、个人资料等一致：优先 {@code accountUserId}，否则 {@code relatedId}。 */
    public Integer getPrimaryUserIdFromToken(String token) {
        Integer accountId = getAccountUserIdFromToken(token);
        if (accountId != null) {
            return accountId;
        }
        return getRelatedIdFromToken(token);
    }
}
