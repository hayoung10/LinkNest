package com.linknest.backend.auth.token;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(String key, RefreshTokenEntity entity, Duration ttl);
    Optional<RefreshTokenEntity> find(String key);
    void delete(String key);
}
