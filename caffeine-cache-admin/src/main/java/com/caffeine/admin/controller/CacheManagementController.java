package com.caffeine.admin.controller;

import com.caffeine.component.CacheConfig;
import com.caffeine.component.CaffeineCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理控制器
 */
@Controller
@RequestMapping("/cache")
public class CacheManagementController {
    // 缓存管理器映射
    private final Map<String, CaffeineCacheManager> cacheManagers = new ConcurrentHashMap<>();
    // 配置存储
    private final Map<String, CacheConfig> cacheConfigs = new ConcurrentHashMap<>();

    /**
     * 缓存管理首页
     */
    @GetMapping("/manage")
    public String manage(Model model) {
        model.addAttribute("cacheConfigs", cacheConfigs);
        return "cache/manage";
    }

    /**
     * 查看缓存详情
     */
    @GetMapping("/detail/{cacheName}")
    public String detail(@PathVariable String cacheName, Model model) {
        CacheConfig config = cacheConfigs.get(cacheName);
        if (config == null) {
            return "redirect:/cache/manage";
        }

        model.addAttribute("cacheName", cacheName);
        model.addAttribute("config", config);
        return "cache/detail";
    }

    /**
     * 更新缓存配置
     */
    @PostMapping("/update/{cacheName}")
    public String update(@PathVariable String cacheName,
                         @RequestParam boolean offHeapCacheEnabled,
                         @RequestParam boolean persistenceEnabled,
                         @RequestParam boolean asyncWarmupEnabled,
                         @RequestParam long maximumSize,
                         @RequestParam long expireAfterWrite) {
        CacheConfig config = cacheConfigs.get(cacheName);
        if (config != null) {
            config.setOffHeapCacheEnabled(offHeapCacheEnabled);
            config.setPersistenceEnabled(persistenceEnabled);
            config.setAsyncWarmupEnabled(asyncWarmupEnabled);
            config.setMaximumSize(maximumSize);
            config.setExpireAfterWrite(expireAfterWrite);

            // 更新缓存管理器配置
            CaffeineCacheManager cacheManager = cacheManagers.get(cacheName);
            if (cacheManager != null) {
                cacheManager.setConfig(config);
            }
        }

        return "redirect:/cache/detail/" + cacheName;
    }

    /**
     * 注册缓存管理器
     */
    @PostMapping("/register")
    @ResponseBody
    public String registerCache(@RequestParam String cacheName,
                               @RequestParam(required = false, defaultValue = "10000") long maximumSize,
                               @RequestParam(required = false, defaultValue = "3600") long expireAfterWrite) {
        if (cacheConfigs.containsKey(cacheName)) {
            return "缓存名称已存在";
        }

        // 创建配置
        CacheConfig config = new CacheConfig();
        config.setMaximumSize(maximumSize);
        config.setExpireAfterWrite(expireAfterWrite);
        cacheConfigs.put(cacheName, config);

        // 创建缓存管理器
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(config);
        cacheManagers.put(cacheName, cacheManager);

        return "缓存注册成功";
    }

    /**
     * 刷新缓存
     */
    @PostMapping("/refresh/{cacheName}")
    @ResponseBody
    public String refreshCache(@PathVariable String cacheName) {
        CaffeineCacheManager cacheManager = cacheManagers.get(cacheName);
        if (cacheManager == null) {
            return "缓存不存在";
        }

        // 持久化缓存
        if (cacheManager.getConfig().isPersistenceEnabled()) {
            cacheManager.persistCache(cacheName);
        }

        // 保存到堆外缓存
        if (cacheManager.getConfig().isOffHeapCacheEnabled()) {
            cacheManager.saveToOffHeapCache(cacheName);
        }

        return "缓存刷新成功";
    }
}