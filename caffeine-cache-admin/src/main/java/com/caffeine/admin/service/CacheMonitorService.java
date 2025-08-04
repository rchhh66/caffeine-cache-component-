package com.caffeine.admin.service;

import com.caffeine.admin.model.CacheMonitor;
import com.caffeine.admin.repository.CacheMonitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存监控服务，用于收集和分析缓存性能指标
 */
@Service
public class CacheMonitorService {
    @Autowired
    private CacheMonitorRepository cacheMonitorRepository;

    // 内存缓存，用于提高性能
    private final Map<String, CacheMonitor> monitorCache = new ConcurrentHashMap<>();

    /**
     * 初始化：从数据库加载监控数据
     */
    @PostConstruct
    public void init() {
        cacheMonitorRepository.findAll().forEach(monitor -> {
            monitorCache.put(monitor.getCacheName(), monitor);
        });
    }

    /**
     * 记录缓存访问
     */
    public void recordAccess(String cacheName, boolean hit, long accessTime) {
        CacheMonitor monitor = getOrCreateMonitor(cacheName);

        // 更新访问计数
        monitor.setAccessCount(monitor.getAccessCount() + 1);

        // 更新命中/未命中计数
        if (hit) {
            monitor.setHitCount(monitor.getHitCount() + 1);
        } else {
            monitor.setMissCount(monitor.getMissCount() + 1);
        }

        // 更新平均访问时间
        long newAvg = (long)((monitor.getAverageAccessTime() * (monitor.getAccessCount() - 1) + accessTime) / monitor.getAccessCount());
        monitor.setAverageAccessTime(newAvg);

        // 更新最后访问时间
        monitor.setLastAccessTime(LocalDateTime.now());

        // 保存到数据库
        cacheMonitorRepository.save(monitor);

        // 更新内存缓存
        monitorCache.put(cacheName, monitor);
    }

    /**
     * 记录缓存过期
     */
    public void recordExpire(String cacheName) {
        CacheMonitor monitor = getOrCreateMonitor(cacheName);
        monitor.setExpireCount(monitor.getExpireCount() + 1);
        cacheMonitorRepository.save(monitor);
        monitorCache.put(cacheName, monitor);
    }

    /**
     * 记录缓存移除
     */
    public void recordEviction(String cacheName) {
        CacheMonitor monitor = getOrCreateMonitor(cacheName);
        monitor.setEvictionCount(monitor.getEvictionCount() + 1);
        cacheMonitorRepository.save(monitor);
        monitorCache.put(cacheName, monitor);
    }

    /**
     * 更新缓存大小
     */
    public void updateCacheSize(String cacheName, int size) {
        CacheMonitor monitor = getOrCreateMonitor(cacheName);
        monitor.setCacheSize(size);
        cacheMonitorRepository.save(monitor);
        monitorCache.put(cacheName, monitor);
    }

    /**
     * 获取或创建缓存监控对象
     */
    private CacheMonitor getOrCreateMonitor(String cacheName) {
        // 先检查内存缓存
        CacheMonitor monitor = monitorCache.get(cacheName);
        if (monitor != null) {
            return monitor;
        }

        // 从数据库查找
        Optional<CacheMonitor> optionalMonitor = cacheMonitorRepository.findByCacheName(cacheName);
        if (optionalMonitor.isPresent()) {
            monitor = optionalMonitor.get();
            monitorCache.put(cacheName, monitor);
            return monitor;
        }

        // 创建新的监控对象
        monitor = new CacheMonitor();
        monitor.setCacheName(cacheName);
        monitor.setAccessCount(0);
        monitor.setHitCount(0);
        monitor.setMissCount(0);
        monitor.setExpireCount(0);
        monitor.setEvictionCount(0);
        monitor.setCacheSize(0);
        monitor.setAverageAccessTime(0);
        monitor.setLastAccessTime(LocalDateTime.now());

        // 保存到数据库
        cacheMonitorRepository.save(monitor);
        monitorCache.put(cacheName, monitor);
        return monitor;
    }

    /**
     * 获取缓存性能指标
     */
    public Map<String, Object> getCacheMetrics(String cacheName) {
        CacheMonitor monitor = getOrCreateMonitor(cacheName);
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("accessCount", monitor.getAccessCount());
        metrics.put("hitCount", monitor.getHitCount());
        metrics.put("missCount", monitor.getMissCount());
        metrics.put("cacheSize", monitor.getCacheSize());
        metrics.put("expireCount", monitor.getExpireCount());
        metrics.put("evictionCount", monitor.getEvictionCount());
        metrics.put("averageAccessTime", monitor.getAverageAccessTime());
        metrics.put("lastAccessTime", monitor.getLastAccessTime());

        // 计算命中率
        double hitRate = monitor.getAccessCount() > 0 ? (double) monitor.getHitCount() / monitor.getAccessCount() : 0;
        metrics.put("hitRate", hitRate);

        return metrics;
    }

    /**
     * 获取所有缓存的性能指标
     */
    public Map<String, Map<String, Object>> getAllCacheMetrics() {
        Map<String, Map<String, Object>> allMetrics = new HashMap<>();
        cacheMonitorRepository.findAll().forEach(monitor -> {
            allMetrics.put(monitor.getCacheName(), getCacheMetrics(monitor.getCacheName()));
        });
        return allMetrics;
    }
}