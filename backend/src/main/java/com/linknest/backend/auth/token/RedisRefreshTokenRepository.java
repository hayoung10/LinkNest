package com.linknest.backend.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {
    @Qualifier("refreshTokenRedisTemplate")
    private final RedisTemplate<String, RefreshTokenEntity> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String RT_PREFIX = "rt:"; // 개별 rt
    private static final String USER_RT_PREFIX = "user-rt:"; // user별 jti set

    private String rtKey(String jti) {
        return RT_PREFIX + jti;
    }

    private String userRtKey(Long userId) {
        return USER_RT_PREFIX + userId;
    }

    @Override
    public void save(RefreshTokenEntity entity, Duration ttl) {
        String jtiKey = rtKey(entity.getId());
        String userKey = userRtKey(entity.getUserId());

        // 개별 RT 저장
        redisTemplate.opsForValue().set(jtiKey, entity, ttl);

        // user-rt:{userId} 집합에 jti 추가
        stringRedisTemplate.opsForSet().add(userKey, entity.getId());
    }

    @Override
    public Optional<RefreshTokenEntity> find(String jti) {
        RefreshTokenEntity entity = redisTemplate.opsForValue().get(rtKey(jti));
        return Optional.ofNullable(entity);
    }

    @Override
    public void delete(String jti) {
        String jtiKey = rtKey(jti);
        RefreshTokenEntity entity = redisTemplate.opsForValue().get(jtiKey);

        // 개별 RT 삭제
        redisTemplate.delete(jtiKey);

        // user-rt:{userId} 집합에서 jti 삭제
        if(entity != null) {
            String userKey = userRtKey(entity.getUserId());
            stringRedisTemplate.opsForSet().remove(userKey, entity.getId());
        }
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        String userKey = userRtKey(userId);

        Set<String> jtis = stringRedisTemplate.opsForSet().members(userKey);
        if(jtis != null && !jtis.isEmpty()) {
            // rt:{jti} 전체 삭제
            List<String> keys = jtis.stream()
                    .map(this::rtKey)
                    .toList();
            redisTemplate.delete(keys);
        }

        // user-rt:{userId} Set 삭제
        stringRedisTemplate.delete(userKey);
    }
}
