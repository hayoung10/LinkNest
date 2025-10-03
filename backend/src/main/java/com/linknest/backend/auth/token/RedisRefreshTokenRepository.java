package com.linknest.backend.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {
    @Qualifier("refreshTokenRedisTemplate")
    private final RedisTemplate<String, RefreshTokenEntity> template;

    @Override
    public void save(String key, RefreshTokenEntity entity, Duration ttl) {
        template.opsForValue().set(key, entity, ttl);
    }

    @Override
    public Optional<RefreshTokenEntity> find(String key) {
        return Optional.ofNullable(template.opsForValue().get(key));
    }

    @Override
    public void delete(String key) {
        template.delete(key);
    }
}
