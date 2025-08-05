package com.caffeine.component.listener;

import com.caffeine.component.model.CacheConfig;

/**
 * 缓存配置变更监听器接口
 */
public interface CacheConfigChangeListener {
    
    /**
     * 当缓存配置变更时调用
     * @param config 变更后的缓存配置
     */
    void onCacheConfigChanged(CacheConfig config);
}