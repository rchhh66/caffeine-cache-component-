package com.caffeine.test;

import com.caffeine.component.CacheConfig;
import com.caffeine.component.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Cache;

public class CacheTestApp {
    public static void main(String[] args) throws InterruptedException {
        // 创建缓存配置
        CacheConfig config = new CacheConfig();
        config.setMaximumSize(1000);
        config.setExpireAfterWrite(60); // 60秒过期

        // 创建缓存管理器
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(config);

        // 获取缓存实例
        Cache<Object, Object> cache = cacheManager.getCache("testCache");

        // 测试基本缓存操作
        System.out.println("测试基本缓存操作:");
        cache.put("key1", "value1");
        System.out.println("key1的值: " + cache.getIfPresent("key1"));

        // 测试配置更新
        System.out.println("\n测试配置更新:");
        CacheConfig newConfig = new CacheConfig();
        newConfig.setMaximumSize(2000);
        newConfig.setExpireAfterWrite(120); // 更新为120秒过期

        System.out.println("更新前的最大容量: " + cacheManager.getConfig().getMaximumSize());
        System.out.println("更新前的过期时间: " + cacheManager.getConfig().getExpireAfterWrite());

        // 更新配置
        cacheManager.setConfig(newConfig);

        System.out.println("更新后的最大容量: " + cacheManager.getConfig().getMaximumSize());
        System.out.println("更新后的过期时间: " + cacheManager.getConfig().getExpireAfterWrite());

        // 验证缓存仍然可用且数据未丢失
        System.out.println("更新后key1的值: " + cache.getIfPresent("key1"));

        // 等待一段时间，观察是否会重建缓存
        System.out.println("\n等待5秒...");
        Thread.sleep(5000);

        // 再次验证缓存
        System.out.println("等待后key1的值: " + cache.getIfPresent("key1"));
    }
}