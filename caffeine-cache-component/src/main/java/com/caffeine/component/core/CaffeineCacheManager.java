package com.caffeine.component.core;

import com.caffeine.component.config.CacheConfig;
import com.caffeine.component.listener.CacheConfigChangeListener;
import com.caffeine.component.offheap.OffHeapCacheManager;
import com.caffeine.component.persistence.CachePersistenceManager;
import com.caffeine.component.persistence.CacheWriterAdapter;
import com.caffeine.component.persistence.CacheWithWriter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine缓存管理器
 */
public class CaffeineCacheManager implements CacheConfigChangeListener {
    // 缓存实例容器
    private final Map<String, Cache<Object, Object>> cacheContainer = new ConcurrentHashMap<>();
    // 配置
    private final CacheConfig config;
    // 堆外缓存管理器
    private final OffHeapCacheManager offHeapCacheManager;
    // 缓存持久化管理器
    private final CachePersistenceManager persistenceManager;

    /**
     * 构造函数
     * @param config 缓存配置
     */
    public CaffeineCacheManager(CacheConfig config) {
        this.config = config;
        this.offHeapCacheManager = config.isOffHeapCacheEnabled() ? new OffHeapCacheManager(config) : null;
        this.persistenceManager = config.isPersistenceEnabled() ? new CachePersistenceManager(config) : null;
    }

    /**
     * 获取或创建缓存
     * @param cacheName 缓存名称
     * @return 缓存实例
     */
    public Cache<Object, Object> getCache(String cacheName) {
        return cacheContainer.computeIfAbsent(cacheName, name -> {
            Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                    .maximumSize(config.getMaximumSize())
                    .expireAfterWrite(config.getExpireAfterWrite(), TimeUnit.SECONDS);

            Cache<Object, Object> cache = caffeineBuilder.build();

            // 如果启用了自动持久化，则添加写监听器
            if (config.isAutoPersistenceEnabled()) {
                CacheWriterAdapter<Object, Object> writerAdapter = new CacheWriterAdapter<>(cacheName, persistenceManager);
                // 包装缓存以添加写入逻辑
                cache = new CacheWithWriter<>(cache, writerAdapter);
            }

            // 从持久化存储加载缓存（如果启用）
            if (config.isPersistenceEnabled() && persistenceManager != null) {
                persistenceManager.loadCache(cacheName, cache);
            }

            // 从堆外缓存加载（如果启用）
            if (config.isOffHeapCacheEnabled() && offHeapCacheManager != null) {
                offHeapCacheManager.loadToHeapCache(cacheName, cache);
            }

            return cache;
        });
    }

    /**
     * 保存缓存到堆外存储
     * @param cacheName 缓存名称
     */
    public void saveToOffHeapCache(String cacheName) {
        if (config.isOffHeapCacheEnabled() && offHeapCacheManager != null && cacheContainer.containsKey(cacheName)) {
            Cache<Object, Object> cache = cacheContainer.get(cacheName);
            offHeapCacheManager.saveFromHeapCache(cacheName, cache);
        }
    }

    /**
     * 保存所有缓存到堆外存储
     */
    public void saveAllToOffHeapCache() {
        if (config.isOffHeapCacheEnabled()) {
            cacheContainer.keySet().forEach(this::saveToOffHeapCache);
        }
    }

    /**
     * 持久化缓存到本地磁盘
     * @param cacheName 缓存名称
     */
    public void persistCache(String cacheName) {
        if (config.isPersistenceEnabled() && persistenceManager != null && cacheContainer.containsKey(cacheName)) {
            Cache<Object, Object> cache = cacheContainer.get(cacheName);
            persistenceManager.persistCache(cacheName, cache);
        }
    }

    /**
     * 持久化所有缓存到本地磁盘
     */
    public void persistAllCaches() {
        if (config.isPersistenceEnabled()) {
            cacheContainer.keySet().forEach(this::persistCache);
        }
    }

    /**
     * 设置缓存配置
     * @param config 缓存配置
     */
    public void setConfig(CacheConfig config) {
        // 保存旧配置以便比较
        CacheConfig oldConfig = new CacheConfig();
        oldConfig.update(this.config);
        
        // 更新配置
        this.config.update(config);
        
        // 检查关键配置是否变更
        if (config.getMaximumSize() != oldConfig.getMaximumSize() ||
            config.getExpireAfterWrite() != oldConfig.getExpireAfterWrite() ||
            config.isAutoPersistenceEnabled() != oldConfig.isAutoPersistenceEnabled() ||
            config.isOffHeapCacheEnabled() != oldConfig.isOffHeapCacheEnabled() ||
            config.isPersistenceEnabled() != oldConfig.isPersistenceEnabled()) {
            
            // 重建所有缓存以应用新配置
            rebuildAllCaches();
        }
    }

    /**
     * 处理配置变更事件
     * @param newConfig 新配置
     */
    public void onConfigChanged(CacheConfig newConfig) {
        setConfig(newConfig);
        System.out.println("缓存配置已更新并应用: " + newConfig.getName());
    }

    @Override
    public void onCacheConfigChanged(CacheConfig config) {
        if (config != null && config.getName().equals(this.config.getName())) {
            onConfigChanged(config);
        }
    }
    
    /**
     * 重建所有缓存
     */
    private void rebuildAllCaches() {
        // 保存所有缓存数据
        Map<String, Map<Object, Object>> allCacheData = new ConcurrentHashMap<>();
        cacheContainer.forEach((cacheName, cache) -> {
            allCacheData.put(cacheName, new ConcurrentHashMap<>(cache.asMap()));
        });
        
        // 清空现有缓存容器
        cacheContainer.clear();
        
        // 重建缓存并恢复数据
        allCacheData.forEach((cacheName, data) -> {
            Cache<Object, Object> newCache = getCache(cacheName);
            newCache.putAll(data);
        });
    }

    /**
     * 初始化缓存
     */
    private void initCaches() {
        // 初始化默认缓存
        getCache(config.getName());
    }

    /**
     * 获取缓存配置
     * @return 缓存配置对象
     */
    public CacheConfig getConfig() {
        return this.config;
    }
}