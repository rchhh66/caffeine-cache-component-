package com.caffeine.admin.service;

import com.caffeine.admin.service.CaffeineCacheManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存监控服务，用于收集和分析缓存性能指标
 */
@Service
public class CacheMonitorService {
    // 缓存访问次数
    private final Map<String, AtomicLong> accessCount = new ConcurrentHashMap<>();
    // 缓存命中次数
    private final Map<String, AtomicLong> hitCount = new ConcurrentHashMap<>();
    // 缓存未命中次数
    private final Map<String, AtomicLong> missCount = new ConcurrentHashMap<>();
    // 缓存大小
    private final Map<String, Integer> cacheSize = new ConcurrentHashMap<>();
    // 缓存过期次数
    private final Map<String, AtomicLong> expireCount = new ConcurrentHashMap<>();
    // 缓存移除次数
    private final Map<String, AtomicLong> evictionCount = new ConcurrentHashMap<>();
    // 缓存平均访问时间(毫秒)
    private final Map<String, AtomicLong> averageAccessTime = new ConcurrentHashMap<>();

    /**
     * 记录缓存访问
     */
    public void recordAccess(String cacheName, boolean hit, long accessTime) {
        accessCount.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        if (hit) {
            hitCount.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        } else {
            missCount.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        }

        // 更新平均访问时间
        AtomicLong currentAvg = averageAccessTime.computeIfAbsent(cacheName, k -> new AtomicLong(0));
        long currentValue = currentAvg.get();
        long newAvg = (currentValue * (accessCount.get(cacheName).get() - 1) + accessTime) / accessCount.get(cacheName).get();
        currentAvg.set(newAvg);
    }

    /**
     * 记录缓存过期
     */
    public void recordExpire(String cacheName) {
        expireCount.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录缓存移除
     */
    public void recordEviction(String cacheName) {
        evictionCount.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 更新缓存大小
     */
    public void updateCacheSize(String cacheName, int size) {
        cacheSize.put(cacheName, size);
    }

    /**
     * 获取缓存性能指标
     */
    public Map<String, Object> getCacheMetrics(String cacheName) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("accessCount", accessCount.getOrDefault(cacheName, new AtomicLong(0)).get());
        metrics.put("hitCount", hitCount.getOrDefault(cacheName, new AtomicLong(0)).get());
        metrics.put("missCount", missCount.getOrDefault(cacheName, new AtomicLong(0)).get());
        metrics.put("cacheSize", cacheSize.getOrDefault(cacheName, 0));
        metrics.put("expireCount", expireCount.getOrDefault(cacheName, new AtomicLong(0)).get());
        metrics.put("evictionCount", evictionCount.getOrDefault(cacheName, new AtomicLong(0)).get());
        metrics.put("averageAccessTime", averageAccessTime.getOrDefault(cacheName, new AtomicLong(0)).get());

        // 计算命中率
        long access = accessCount.getOrDefault(cacheName, new AtomicLong(0)).get();
        long hit = hitCount.getOrDefault(cacheName, new AtomicLong(0)).get();
        metrics.put("hitRate", access > 0 ? (double) hit / access : 0);

        return metrics;
    }

    /**
     * 获取所有缓存的性能指标
     */
    public Map<String, Map<String, Object>> getAllCacheMetrics() {
        Map<String, Map<String, Object>> allMetrics = new HashMap<>();
        accessCount.keySet().forEach(cacheName -> {
            allMetrics.put(cacheName, getCacheMetrics(cacheName));
        });
        return allMetrics;
    }
}