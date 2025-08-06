package com.caffeine.test;

import com.caffeine.component.core.CaffeineCacheManager;
import com.caffeine.component.listener.CacheConfigChangeNotifier;
import com.caffeine.component.listener.CacheConfigChangeListener;
import com.caffeine.component.config.CacheConfig;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CacheConfigDynamicUpdateTest {

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
        initialConfig.setExpireAfterWrite(60); // 60秒

        // 创建缓存管理器并注册到通知器
        cacheManager = new CaffeineCacheManager(initialConfig);
        configChangeNotifier.registerListener(cacheManager);
    }

    @Test
    public void testConfigDynamicUpdate() {
        // 验证初始配置
        assertEquals(100, cacheManager.getConfig().getMaximumSize());
        assertEquals(60, cacheManager.getConfig().getExpireAfterWrite());

        // 创建新的缓存配置
        CacheConfig newConfig = new CacheConfig();
        newConfig.setName("testCache");
        newConfig.setMaximumSize(200);
        newConfig.setExpireAfterWrite(120); // 120秒

        // 模拟配置变更通知
        configChangeNotifier.notifyConfigChange(newConfig);

        // 验证配置是否更新
        assertEquals(200, cacheManager.getConfig().getMaximumSize());
        assertEquals(120, cacheManager.getConfig().getExpireAfterWrite());

        // 验证缓存是否按新配置工作
        // 这里可以添加更多测试，例如验证缓存大小限制和过期时间
    }

    @Test
    public void testUnrelatedConfigUpdate() {
        // 创建与当前缓存无关的配置
        CacheConfig unrelatedConfig = new CacheConfig();
        unrelatedConfig.setName("otherCache");
        unrelatedConfig.setMaximumSize(300);
        unrelatedConfig.setExpireAfterWrite(180);

        // 模拟配置变更通知
        configChangeNotifier.notifyConfigChange(unrelatedConfig);

        // 验证当前缓存配置未被修改
        assertEquals(100, cacheManager.getConfig().getMaximumSize());
        assertEquals(60, cacheManager.getConfig().getExpireAfterWrite());
    }

    @Test
    public void testListenerRegistrationAndRemoval() {
        // 创建新的缓存管理器
        CacheConfig anotherConfig = new CacheConfig();
        anotherConfig.setName("anotherCache");
        anotherConfig.setMaximumSize(100);
        anotherConfig.setExpireAfterWrite(60);
        CaffeineCacheManager anotherManager = new CaffeineCacheManager(anotherConfig);

        // 注册监听器
        configChangeNotifier.registerListener(anotherManager);

        // 创建一个新的配置并通知变更
        CacheConfig updateConfig = new CacheConfig();
        updateConfig.setName("anotherCache");
        updateConfig.setMaximumSize(200);
        updateConfig.setExpireAfterWrite(120);
        configChangeNotifier.notifyConfigChange(updateConfig);

        // 验证另一个管理器的配置是否被更新
        assertEquals(200, anotherManager.getConfig().getMaximumSize());
        assertEquals(120, anotherManager.getConfig().getExpireAfterWrite());

        // 移除监听器
        configChangeNotifier.removeListener(anotherManager);

        // 再次更新配置
        CacheConfig updateConfig2 = new CacheConfig();
        updateConfig2.setName("anotherCache");
        updateConfig2.setMaximumSize(300);
        updateConfig2.setExpireAfterWrite(180);
        configChangeNotifier.notifyConfigChange(updateConfig2);

        // 验证另一个管理器的配置是否不再被更新
        assertEquals(200, anotherManager.getConfig().getMaximumSize());
        assertEquals(120, anotherManager.getConfig().getExpireAfterWrite());
    }
}