package com.caffeine.admin.service;

import com.caffeine.admin.model.CacheConfig;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class CaffeineCacheManager {
    private final Map<String, Cache<Object, Object>> caches = new ConcurrentHashMap<>();
    private final CacheMonitorService cacheMonitorService;

    public CaffeineCacheManager(CacheMonitorService cacheMonitorService) {
        this.cacheMonitorService = cacheMonitorService;
    }

    public void createCache(CacheConfig config) {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .initialCapacity(config.getInitialCapacity())
                .maximumSize(config.getMaximumSize());

        // 根据过期策略配置
        if ("expireAfterWrite".equals(config.getExpirePolicy())) {
            caffeineBuilder.expireAfterWrite(config.getExpireTime(), TimeUnit.MILLISECONDS);
        } else if ("expireAfterAccess".equals(config.getExpirePolicy())) {
            caffeineBuilder.expireAfterAccess(config.getExpireTime(), TimeUnit.MILLISECONDS);
        }

        // 创建缓存
        Cache<Object, Object> cache = caffeineBuilder.build();
        caches.put(config.getCacheName(), cache);
    }

    public Cache<Object, Object> getCache(String cacheName) {
        return caches.get(cacheName);
    }

    public void evictCache(String cacheName) {
        Cache<Object, Object> cache = caches.get(cacheName);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    public void evictCacheKey(String cacheName, Object key) {
        Cache<Object, Object> cache = caches.get(cacheName);
        if (cache != null) {
            cache.invalidate(key);
            cacheMonitorService.recordEviction(cacheName);
        }
    }

    public void put(String cacheName, Object key, Object value) {
        Cache<Object, Object> cache = caches.get(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    public Object get(String cacheName, Object key) {
        long startTime = System.nanoTime();
        Cache<Object, Object> cache = caches.get(cacheName);
        if (cache != null) {
            Object value = cache.getIfPresent(key);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            cacheMonitorService.recordAccess(cacheName, value != null, duration);
            return value;
        }
        return null;
    }

    public int size(String cacheName) {
        Cache<Object, Object> cache = caches.get(cacheName);
        if (cache != null) {
            return (int) cache.estimatedSize();
        }
        return 0;
    }

    public boolean exists(String cacheName) {
        return caches.containsKey(cacheName);
    }

    public void removeCache(String cacheName) {
        caches.remove(cacheName);
    }
}