package com.caffeine.admin.service;

import com.caffeine.admin.model.CacheConfig;
import com.caffeine.admin.repository.CacheConfigRepository;
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
    // 构造函数注入
    @Autowired
    public CacheConfigService(CacheConfigRepository cacheConfigRepository) {
        this.cacheConfigRepository = cacheConfigRepository;
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
     * 实际应用中，这里应该通过消息队列或WebSocket通知客户端
     */
    private void notifyConfigChange(String cacheName, CacheConfig newConfig) {
        // 模拟通知逻辑
        System.out.println("通知应用: 缓存 '" + cacheName + "' 的配置已更新");
        // 实际实现中，这里可以通过Spring Cloud Bus、WebSocket或其他消息机制发送通知
    }
}