package com.linknest.backend.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Profile("dev")
@RestController
public class RedisHealthController {
    private final StringRedisTemplate redisTemplate;

    public RedisHealthController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/api/redis/ping")
    public Map<String, String> pingRedis() {
        String key = "redisnest:ping";
        redisTemplate.opsForValue().set(key, "pong");
        String value = redisTemplate.opsForValue().get(key);
        return Map.of("redis", "pong".equals(value) ? "OK" : "FAIL");
    }
}
