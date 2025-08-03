package com.caffeine.component.offheap;

import com.caffeine.component.config.CacheConfig;
import com.github.benmanes.caffeine.cache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * 堆外缓存管理器
 */
public class OffHeapCacheManager {
    // 缓存配置
    private final CacheConfig config;
    // EHCache管理器
    private final CacheManager cacheManager;
    // 缓存映射
    private final Map<String, org.ehcache.Cache<Object, Object>> offHeapCaches;

    /**
     * 构造函数
     * @param config 缓存配置
     */
    public OffHeapCacheManager(CacheConfig config) {
        this.config = config;
        this.offHeapCaches = new HashMap<>();

        // 初始化EHCache管理器
        this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("default", CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class, Object.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .offheap(1, MemoryUnit.GB))) // 默认堆外内存大小
                .build();
        this.cacheManager.init();
    }

    /**
     * 从堆缓存保存到堆外缓存
     * @param cacheName 缓存名称
     * @param heapCache 堆缓存
     */
    public void saveFromHeapCache(String cacheName, Cache<Object, Object> heapCache) {
        if (!config.isOffHeapCacheEnabled()) {
            return;
        }

        // 获取或创建堆外缓存
        org.ehcache.Cache<Object, Object> offHeapCache = getOrCreateOffHeapCache(cacheName);

        // 清空堆外缓存
        offHeapCache.clear();

        // 将堆缓存中的数据保存到堆外缓存
        heapCache.asMap().forEach((key, value) -> {
            offHeapCache.put(key, value);
        });
    }

    /**
     * 从堆外缓存加载到堆缓存
     * @param cacheName 缓存名称
     * @param heapCache 堆缓存
     */
    public void loadToHeapCache(String cacheName, Cache<Object, Object> heapCache) {
        if (!config.isOffHeapCacheEnabled()) {
            return;
        }

        // 获取堆外缓存
        org.ehcache.Cache<Object, Object> offHeapCache = getOffHeapCache(cacheName);
        if (offHeapCache == null) {
            return;
        }

        // 将堆外缓存中的数据加载到堆缓存
        offHeapCache.forEach(entry -> {
            heapCache.put(entry.getKey(), entry.getValue());
        });
    }

    /**
     * 获取或创建堆外缓存
     * @param cacheName 缓存名称
     * @return 堆外缓存
     */
    private org.ehcache.Cache<Object, Object> getOrCreateOffHeapCache(String cacheName) {
        return offHeapCaches.computeIfAbsent(cacheName, name -> {
            // 创建堆外缓存配置
            CacheConfiguration<Object, Object> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    Object.class, Object.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                            .offheap(config.getMaximumSize() * 1024, MemoryUnit.KB)) // 假设每个条目平均1KB
                    .build();

            // 创建堆外缓存
            return cacheManager.createCache(name, cacheConfig);
        });
    }

    /**
     * 获取堆外缓存
     * @param cacheName 缓存名称
     * @return 堆外缓存，如果不存在则返回null
     */
    private org.ehcache.Cache<Object, Object> getOffHeapCache(String cacheName) {
        return offHeapCaches.get(cacheName);
    }

    /**
     * 关闭缓存管理器
     */
    public void shutdown() {
        cacheManager.close();
    }
}