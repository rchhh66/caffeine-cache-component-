package com.caffeine.admin.service;

import com.caffeine.admin.service.CaffeineCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存内容服务，用于管理和查看缓存中的内容
 */
@Service
public class CacheContentService {
    // 缓存管理器映射
    private final Map<String, CaffeineCacheManager> cacheManagers = new ConcurrentHashMap<>();
    // 缓存监控服务
    private final CacheMonitorService cacheMonitorService;

    @Autowired
    public CacheContentService(CacheMonitorService cacheMonitorService) {
        this.cacheMonitorService = cacheMonitorService;
    }

    /**
     * 注册缓存管理器
     */
    public void registerCacheManager(String cacheName, CaffeineCacheManager cacheManager) {
        cacheManagers.put(cacheName, cacheManager);
    }

    /**
     * 获取缓存管理器
     */
    public CaffeineCacheManager getCacheManager(String cacheName) {
        return cacheManagers.get(cacheName);
    }

    /**
     * 获取所有缓存名称
     */
    public Set<String> getAllCacheNames() {
        return new HashSet<>(cacheManagers.keySet());
    }

    /**
     * 获取缓存中的所有key
     * 注意：Caffeine缓存本身不支持获取所有key，这里返回空集合
     * 实际应用中，可以考虑维护一个额外的key集合
     */
    public Set<Object> getCacheKeys(String cacheName) {
        CaffeineCacheManager cacheManager = cacheManagers.get(cacheName);
        if (cacheManager != null) {
            long startTime = System.currentTimeMillis();
            // Caffeine缓存不支持获取所有key，这里返回空集合
            Set<Object> keys = Collections.emptySet();
            long endTime = System.currentTimeMillis();
            // 记录访问
            cacheMonitorService.recordAccess(cacheName, !keys.isEmpty(), endTime - startTime);
            return keys;
        }
        return Collections.emptySet();
    }

    /**
     * 获取缓存中指定key的值
     */
    public Object getCacheValue(String cacheName, Object key) {
        CaffeineCacheManager cacheManager = cacheManagers.get(cacheName);
        if (cacheManager != null) {
            long startTime = System.currentTimeMillis();
            Object value = cacheManager.get(cacheName, key);
            long endTime = System.currentTimeMillis();
            // 记录访问
            cacheMonitorService.recordAccess(cacheName, value != null, endTime - startTime);
            return value;
        }
        return null;
    }

    /**
     * 删除缓存中的指定key
     */
    public boolean removeCacheKey(String cacheName, Object key) {
        CaffeineCacheManager cacheManager = cacheManagers.get(cacheName);
        if (cacheManager != null) {
            cacheManager.evictCacheKey(cacheName, key);
            // 记录移除
            cacheMonitorService.recordEviction(cacheName);
            // 更新缓存大小
            cacheMonitorService.updateCacheSize(cacheName, cacheManager.size(cacheName));
            return true;
        }
        return false;
    }

    /**
     * 清空整个缓存
     */
    public boolean clearCache(String cacheName) {
        CaffeineCacheManager cacheManager = cacheManagers.get(cacheName);
        if (cacheManager != null) {
            cacheManager.evictCache(cacheName);
            // 记录移除（多次移除）
            int size = cacheManager.size(cacheName);
            for (int i = 0; i < size; i++) {
                cacheMonitorService.recordEviction(cacheName);
            }
            // 更新缓存大小
            cacheMonitorService.updateCacheSize(cacheName, 0);
            return true;
        }
        return false;
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize(String cacheName) {
        CaffeineCacheManager cacheManager = cacheManagers.get(cacheName);
        if (cacheManager != null) {
            return cacheManager.size(cacheName);
        }
        return 0;
    }

    /**
     * 检查缓存管理器是否存在
     */
    public boolean existsCacheManager(String cacheName) {
        return cacheManagers.containsKey(cacheName);
    }
}