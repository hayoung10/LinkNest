package com.linknest.backend.auth.token;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshTokenEntity entity, Duration ttl);
    Optional<RefreshTokenEntity> find(String jti);
    void delete(String jti);
    void deleteAllByUserId(Long userId);
}
