package com.caffeine.admin.service;

import com.caffeine.admin.handler.CacheConfigWebSocketHandler;
import com.caffeine.admin.model.CacheConfig;
import com.caffeine.admin.repository.CacheConfigRepository;
import com.caffeine.component.listener.CacheConfigChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * 缓存配置服务，用于管理缓存场景的配置参数
 */
@Service
public class CacheConfigService {
    // 存储缓存配置
    private final Map<String, CacheConfig> cacheConfigs = new ConcurrentHashMap<>();
    // 缓存配置Repository
    private final CacheConfigRepository cacheConfigRepository;
    // 配置变更通知器
    private final CacheConfigChangeNotifier configChangeNotifier;
    // 构造函数注入
    @Autowired
    public CacheConfigService(CacheConfigRepository cacheConfigRepository, CacheConfigChangeNotifier configChangeNotifier) {
        this.cacheConfigRepository = cacheConfigRepository;
        this.configChangeNotifier = configChangeNotifier;
    }

    /**
     * 初始化：从数据库加载配置
     */
    @PostConstruct
    public void init() {
        // 从数据库加载所有配置
        cacheConfigRepository.findAll().forEach(config -> {
            cacheConfigs.put(config.getCacheName(), config);
        });
    }

    /**
     * 获取所有缓存配置
     */
    public Map<String, CacheConfig> getAllCacheConfigs() {
        return new HashMap<>(cacheConfigs);
    }

    /**
     * 获取指定缓存的配置
     */
    public CacheConfig getCacheConfig(String cacheName) {
        return cacheConfigs.get(cacheName);
    }

    /**
     * 检查缓存配置是否存在
     */
    public boolean existsCacheConfig(String cacheName) {
        return cacheConfigs.containsKey(cacheName);
    }

    /**
     * 更新缓存配置
     */
    public void updateCacheConfig(String cacheName, CacheConfig newConfig) {
        // 保存配置到数据库
        cacheConfigRepository.save(newConfig);

        // 更新内存缓存
        cacheConfigs.put(cacheName, newConfig);

        // 通知应用配置变更
        notifyConfigChange(cacheName, newConfig);
    }

    /**
     * 注册新缓存配置
     */
    public void registerCacheConfig(String cacheName, CacheConfig config) {
        // 检查是否已存在
        if (cacheConfigRepository.existsByCacheName(cacheName)) {
            throw new IllegalArgumentException("缓存配置已存在: " + cacheName);
        }
        updateCacheConfig(cacheName, config);
    }

    /**
     * 通知配置变更
     * 通过WebSocket通知所有客户端配置变更
     * 同时通知服务器端的缓存管理器
     */
    private void notifyConfigChange(String cacheName, CacheConfig newConfig) {
        // 通知服务器端缓存管理器
        configChangeNotifier.notifyConfigChange(newConfig);
        // 通过WebSocket发送配置变更通知给客户端
        CacheConfigWebSocketHandler.sendConfigChangeNotification(cacheName, newConfig);
        System.out.println("已发送配置变更通知: 缓存 '" + cacheName + "'");
    }
}