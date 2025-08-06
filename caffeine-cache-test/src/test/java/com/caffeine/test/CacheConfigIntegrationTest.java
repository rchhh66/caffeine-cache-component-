package com.caffeine.test;

import com.caffeine.component.core.CaffeineCacheManager;
import com.caffeine.component.listener.CacheConfigChangeNotifier;
import com.caffeine.component.config.CacheConfig;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CacheConfigIntegrationTest {

    private CacheConfigChangeNotifier configChangeNotifier;
    private CaffeineCacheManager cacheManager;
    private CacheConfig initialConfig;

    @Before
    public void setUp() {
        // 初始化通知器
        configChangeNotifier = new CacheConfigChangeNotifier();

        // 创建初始缓存配置
        initialConfig = new CacheConfig();
        initialConfig.setName("testCache");
        initialConfig.setMaximumSize(100);
        initialConfig.setExpireAfterWrite(60);

        // 创建缓存管理器
        cacheManager = new CaffeineCacheManager(initialConfig);

        // 注册缓存管理器到通知器
        configChangeNotifier.registerListener(cacheManager);
    }

    @Test
    public void testUpdateCacheConfig() {
        // 创建新的缓存配置
        CacheConfig newConfig = new CacheConfig();
        newConfig.setName("testCache");
        newConfig.setMaximumSize(200);
        newConfig.setExpireAfterWrite(120);

        // 直接调用通知器更新配置
        configChangeNotifier.notifyConfigChange(newConfig);

        // 验证配置是否更新
        assertEquals(200, cacheManager.getConfig().getMaximumSize());
        assertEquals(120, cacheManager.getConfig().getExpireAfterWrite());
    }

    @Test
    public void testCacheDataPreservationAfterConfigUpdate() {
        // 向缓存中添加数据
        cacheManager.getCache("testCache").put("key1", "value1");
        cacheManager.getCache("testCache").put("key2", "value2");

        // 验证数据存在
        assertEquals("value1", cacheManager.getCache("testCache").getIfPresent("key1"));
        assertEquals("value2", cacheManager.getCache("testCache").getIfPresent("key2"));

        // 更新缓存配置
        CacheConfig newConfig = new CacheConfig();
        newConfig.setName("testCache");
        newConfig.setMaximumSize(200);
        newConfig.setExpireAfterWrite(120);
        configChangeNotifier.notifyConfigChange(newConfig);

        // 验证数据仍然存在
        assertEquals("value1", cacheManager.getCache("testCache").getIfPresent("key1"));
        assertEquals("value2", cacheManager.getCache("testCache").getIfPresent("key2"));
    }

    @Test
    public void testMultipleCacheManagersUpdate() {
        // 创建第二个缓存管理器
        CacheConfig secondConfig = new CacheConfig();
        secondConfig.setName("secondCache");
        secondConfig.setMaximumSize(150);
        secondConfig.setExpireAfterWrite(90);
        CaffeineCacheManager secondManager = new CaffeineCacheManager(secondConfig);
        configChangeNotifier.registerListener(secondManager);

        // 更新第一个缓存的配置
        CacheConfig updatedConfig = new CacheConfig();
        updatedConfig.setName("testCache");
        updatedConfig.setMaximumSize(200);
        updatedConfig.setExpireAfterWrite(120);
        configChangeNotifier.notifyConfigChange(updatedConfig);

        // 验证只有第一个缓存管理器的配置被更新
        assertEquals(200, cacheManager.getConfig().getMaximumSize());
        assertEquals(120, cacheManager.getConfig().getExpireAfterWrite());
        assertEquals(150, secondManager.getConfig().getMaximumSize());
        assertEquals(90, secondManager.getConfig().getExpireAfterWrite());
    }
}