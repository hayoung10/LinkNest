package com.linknest.backend.common.redis;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyProvider {
    private final String keyPrefix;

    public RedisKeyProvider(RedisKeyProperties properties) {
        this.keyPrefix = normalizePrefix(properties.keyPrefix());
    }

    // refresh token 단건 key
    public String refreshToken(String jti) { // 개별 rt
        return keyPrefix + "rt:" + jti;
    }

    // user별 refresh token set key
    public String userRefreshTokenSet(Long userId) {
        return keyPrefix + "user-rt:" + userId;
    }

    private String normalizePrefix(String prefix) {
        if(prefix == null || prefix.isBlank()) {
            return "";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
