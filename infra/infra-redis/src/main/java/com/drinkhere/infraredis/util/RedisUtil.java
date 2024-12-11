package com.drinkhere.infraredis.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveAsValue(String key, Object val, Long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, val, time, timeUnit);
    }

    public void saveWithoutExpiration(String key, Object val) {
        redisTemplate.opsForValue().set(key, val);
    }

    public void appendToRecentlyViewedAnnouncement(String key, String newValue) {
        long RECENT_VIEWED_ANNOUNCEMENT_LIMIT = 20;

        Object mostRecentlyViewedValue = redisTemplate.opsForList().index(key, 0);
        if (Objects.equals(mostRecentlyViewedValue, newValue)) {
            return;
        }
        if (Objects.equals(redisTemplate.opsForList().size(key), RECENT_VIEWED_ANNOUNCEMENT_LIMIT)) {
            redisTemplate.opsForList().rightPop(key);
        }
        redisTemplate.opsForList().leftPush(key, newValue);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public List<Object> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
}