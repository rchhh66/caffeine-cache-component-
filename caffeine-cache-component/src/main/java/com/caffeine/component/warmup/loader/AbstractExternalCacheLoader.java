package com.caffeine.component.warmup.loader;

import com.caffeine.component.warmup.CacheDataLoader;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 外部缓存数据加载器抽象基类
 * 提供从外部缓存中间件异步加载数据到本地缓存的通用实现
 */
public abstract class AbstractExternalCacheLoader implements CacheDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(AbstractExternalCacheLoader.class);
    private final ExecutorService executorService;
    private final int threadPoolSize;

    /**
     * 默认构造函数，使用单线程池
     */
    protected AbstractExternalCacheLoader() {
        this(1);
    }

    /**
     * 构造函数，允许指定线程池大小
     * @param threadPoolSize 线程池大小
     */
    protected AbstractExternalCacheLoader(int threadPoolSize) {
        this.threadPoolSize = Math.max(1, threadPoolSize);
        this.executorService = Executors.newFixedThreadPool(this.threadPoolSize);
        logger.info("初始化外部缓存加载器，线程池大小: {}", this.threadPoolSize);
    }

    /**
     * 从外部缓存加载数据到本地缓存
     * @param cacheName 缓存名称
     * @param cache 本地缓存实例
     */
    @Override
    public void loadData(String cacheName, Cache<Object, Object> cache) {
        logger.info("开始异步加载外部缓存数据到本地缓存: {}", cacheName);

        CompletableFuture.runAsync(() -> {
            try {
                doLoadData(cacheName, cache);
                logger.info("成功加载外部缓存数据到本地缓存: {}", cacheName);
            } catch (Exception e) {
                logger.error("加载外部缓存数据到本地缓存失败: {}", cacheName, e);
            }
        }, executorService);
    }

    /**
     * 具体加载数据的实现，由子类提供
     * @param cacheName 缓存名称
     * @param cache 本地缓存实例
     * @throws Exception 加载过程中可能出现的异常
     */
    protected abstract void doLoadData(String cacheName, Cache<Object, Object> cache) throws Exception;

    /**
     * 关闭线程池资源
     */
    public void shutdown() {
        logger.info("关闭外部缓存加载器线程池");
        executorService.shutdown();
    }
}