package com.caffeine.component.config;

import com.caffeine.component.core.CaffeineCacheManager;
import com.caffeine.component.listener.CacheConfigChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 缓存配置监听器配置类
 * 用于将缓存管理器注册到配置变更通知器
 */
@Configuration
public class CacheConfigListenerConfig {
    
    private final ApplicationContext applicationContext;
    private final CacheConfigChangeNotifier configChangeNotifier;
    
    @Autowired
    public CacheConfigListenerConfig(ApplicationContext applicationContext, CacheConfigChangeNotifier configChangeNotifier) {
        this.applicationContext = applicationContext;
        this.configChangeNotifier = configChangeNotifier;
    }
    
    /**
     * 初始化方法：注册所有缓存管理器到通知器
     */
    @PostConstruct
    public void registerCacheManagers() {
        // 获取所有CaffeineCacheManager实例
        Map<String, CaffeineCacheManager> cacheManagers = applicationContext.getBeansOfType(CaffeineCacheManager.class);
        
        // 注册每个缓存管理器为配置变更监听器
        for (CaffeineCacheManager cacheManager : cacheManagers.values()) {
            configChangeNotifier.registerListener(cacheManager);
            System.out.println("已注册缓存管理器: " + cacheManager.getConfig().getCacheName());
        }
    }
}