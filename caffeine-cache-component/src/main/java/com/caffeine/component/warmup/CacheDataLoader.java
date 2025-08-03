package com.caffeine.component.warmup;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * 缓存数据加载器接口
 */
public interface CacheDataLoader {
    /**
     * 加载数据到缓存
     * @param cacheName 缓存名称
     * @param cache 缓存实例
     */
    void loadData(String cacheName, Cache<Object, Object> cache);
}