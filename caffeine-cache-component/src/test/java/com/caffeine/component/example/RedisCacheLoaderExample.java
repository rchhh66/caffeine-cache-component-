package com.caffeine.component.example;

import com.caffeine.component.CacheConfig;
import com.caffeine.component.CaffeineCacheManager;
import com.caffeine.component.loader.RedisCacheLoader;
import com.github.benmanes.caffeine.cache.Cache;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存加载器示例
 */
public class RedisCacheLoaderExample {
    public static void main(String[] args) {
        // 1. 创建缓存配置
        CacheConfig config = new CacheConfig();
        config.setMaximumSize(10000);
        config.setExpireAfterWrite(3600);
        config.setPersistenceEnabled(true);
        config.setAsyncWarmupEnabled(true);

        // 2. 创建缓存管理器
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(config);

        // 3. 获取缓存实例
        String cacheName = "userCache";
        Cache<Object, Object> cache = cacheManager.getCache(cacheName);

        // 4. 初始化Redis缓存加载器
        String redisHost = "localhost";
        int redisPort = 6379;
        String redisPrefix = "app:";

        // 可选：配置Jedis连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);

        RedisCacheLoader redisCacheLoader = new RedisCacheLoader(redisHost, redisPort, redisPrefix, poolConfig);

        // 5. 向Redis添加测试数据（实际应用中，这些数据通常已经存在于Redis中）
        addTestDataToRedis(redisCacheLoader, cacheName);

        // 6. 从Redis异步加载数据到本地缓存
        redisCacheLoader.loadData(cacheName, cache);

        // 7. 等待数据加载完成（实际应用中，这通常是异步的）
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 8. 验证数据是否加载成功
        Object userId1 = cache.getIfPresent("1");
        Object userId2 = cache.getIfPresent("2");

        System.out.println("用户1数据: " + userId1);
        System.out.println("用户2数据: " + userId2);

        // 9. 关闭资源
        redisCacheLoader.shutdown();
    }

    /**
     * 向Redis添加测试数据
     */
    private static void addTestDataToRedis(RedisCacheLoader redisCacheLoader, String cacheName) {
        // 单个设置
        redisCacheLoader.set(cacheName, "1", "{\"id\":1, \"name\":\"张三\", \"age\":30}");
        redisCacheLoader.set(cacheName, "2", "{\"id\":2, \"name\":\"李四\", \"age\":25}");

        // 批量设置
        Map<String, String> batchData = new HashMap<>();
        batchData.put("3", "{\"id\":3, \"name\":\"王五\", \"age\":35}");
        batchData.put("4", "{\"id\":4, \"name\":\"赵六\", \"age\":28}");
        redisCacheLoader.setBatch(cacheName, batchData);
    }
}