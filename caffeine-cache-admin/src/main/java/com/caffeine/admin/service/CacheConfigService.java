package com.caffeine.admin.service;

import com.caffeine.admin.model.CacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 缓存配置服务，用于管理缓存场景的配置参数
 */
@Service
public class CacheConfigService {
    // 存储缓存配置
    private final Map<String, CacheConfig> cacheConfigs = new ConcurrentHashMap<>();
    // 配置文件路径
    @Value("${caffeine.admin.cache.config-path}")
    private String configPath;
    // JSON解析器
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化：从文件加载配置
     */
    @PostConstruct
    public void init() {
        File dir = new File(configPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 加载所有配置文件
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    String cacheName = file.getName().replace(".json", "");
                    CacheConfig config = objectMapper.readValue(file, CacheConfig.class);
                    cacheConfigs.put(cacheName, config);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
        // 保存配置到内存
        cacheConfigs.put(cacheName, newConfig);

        // 保存配置到文件
        try {
            File configFile = new File(configPath + File.separator + cacheName + ".json");
            objectMapper.writeValue(configFile, newConfig);

            // 通知应用配置变更（这里只是模拟，实际应该通过消息队列或WebSocket实现）
            notifyConfigChange(cacheName, newConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册新缓存配置
     */
    public void registerCacheConfig(String cacheName, CacheConfig config) {
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