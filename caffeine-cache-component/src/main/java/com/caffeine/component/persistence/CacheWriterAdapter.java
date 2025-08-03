package com.caffeine.component.persistence;

import com.caffeine.component.persistence.CachePersistenceManager;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存写入适配器，用于自动持久化缓存数据
 */
public class CacheWriterAdapter<K, V> implements RemovalListener<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(CacheWriterAdapter.class);

    // 缓存持久化管理器
    private final CachePersistenceManager persistenceManager;
    // 缓存名称
    private final String cacheName;

    /**
     * 构造函数
     * @param cacheName 缓存名称
     * @param persistenceManager 缓存持久化管理器
     */
    public CacheWriterAdapter(String cacheName, CachePersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.cacheName = cacheName;
    }

    @Override
    public void onRemoval(K key, V value, RemovalCause cause) {
        logger.debug("Removing cache entry: {} for cache: {}", key, cacheName);
        // 处理删除逻辑
        persistenceManager.deleteEntry(cacheName, key);
    }

    public void write(K key, V value) {
        logger.debug("Writing cache entry: {}={} for cache: {}", key, value, cacheName);
        // 持久化单个条目
        persistenceManager.persistEntry(cacheName, key, value);
    }
}