package com.caffeine.component.listener;

import com.caffeine.component.model.CacheConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 缓存配置变更通知器
 */
@Component
public class CacheConfigChangeNotifier {
    
    // 使用线程安全的集合存储监听器
    private final List<CacheConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * 注册监听器
     * @param listener 监听器
     */
    public void registerListener(CacheConfigChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * 移除监听器
     * @param listener 监听器
     */
    public void removeListener(CacheConfigChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
    
    /**
     * 通知所有监听器配置变更
     * @param config 变更后的缓存配置
     */
    public void notifyConfigChange(CacheConfig config) {
        for (CacheConfigChangeListener listener : listeners) {
            try {
                listener.onCacheConfigChanged(config);
            } catch (Exception e) {
                System.err.println("通知配置变更时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}