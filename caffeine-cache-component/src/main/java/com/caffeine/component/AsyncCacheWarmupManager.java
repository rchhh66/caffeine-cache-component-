package com.caffeine.component;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步缓存预热管理器
 */
public class AsyncCacheWarmupManager {
    private static final Logger logger = LoggerFactory.getLogger(AsyncCacheWarmupManager.class);

    // 缓存配置
    private final CacheConfig config;
    // 执行器服务
    private final ExecutorService executorService;
    // 数据加载器
    private CacheDataLoader dataLoader;

    /**
     * 构造函数
     * @param config 缓存配置
     */
    public AsyncCacheWarmupManager(CacheConfig config) {
        this.config = config;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 设置数据加载器
     * @param dataLoader 数据加载器
     */
    public void setDataLoader(CacheDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    /**
     * 异步预热缓存
     * @param cacheName 缓存名称
     * @param cache 缓存实例
     */
    public void warmupCacheAsync(String cacheName, Cache<Object, Object> cache) {
        if (!config.isAsyncWarmupEnabled() || dataLoader == null) {
            return;
        }

        logger.info("开始异步预热缓存: {}", cacheName);

        CompletableFuture.runAsync(() -> {
            try {
                // 调用数据加载器加载数据
                dataLoader.loadData(cacheName, cache);
                logger.info("缓存预热完成: {}", cacheName);
            } catch (Exception e) {
                logger.error("缓存预热失败: {}", cacheName, e);
            }
        }, executorService);
    }

    /**
     * 关闭执行器服务
     */
    public void shutdown() {
        executorService.shutdown();
    }
}