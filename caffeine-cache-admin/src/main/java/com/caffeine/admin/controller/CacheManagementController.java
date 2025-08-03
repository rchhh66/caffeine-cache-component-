package com.caffeine.admin.controller;

import com.caffeine.admin.service.CacheConfigService;
import com.caffeine.admin.service.CacheContentService;
import com.caffeine.admin.service.CacheMonitorService;
import com.caffeine.admin.model.CacheConfig;
import com.caffeine.admin.service.CaffeineCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * 缓存管理控制器
 */
@Controller
@RequestMapping("/cache")
public class CacheManagementController {
    private final CacheContentService cacheContentService;
    private final CacheConfigService cacheConfigService;
    private final CacheMonitorService cacheMonitorService;

    @Autowired
    public CacheManagementController(CacheContentService cacheContentService, CacheConfigService cacheConfigService, CacheMonitorService cacheMonitorService) {
        this.cacheContentService = cacheContentService;
        this.cacheConfigService = cacheConfigService;
        this.cacheMonitorService = cacheMonitorService;
    }

    /**
     * 缓存管理首页
     */
    @GetMapping("/manage")
    public String manage(Model model) {
        model.addAttribute("cacheConfigs", cacheConfigService.getAllCacheConfigs());
        return "cache/manage";
    }

    /**
     * 查看缓存详情
     */
    @GetMapping("/detail/{cacheName}")
    public String detail(@PathVariable String cacheName, Model model) {
        CacheConfig config = cacheConfigService.getCacheConfig(cacheName);
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
        CacheConfig config = cacheConfigService.getCacheConfig(cacheName);
        if (config != null) {
            config.setOffHeap(offHeapCacheEnabled);
            config.setPersistent(persistenceEnabled);
            config.setAsyncWarmup(asyncWarmupEnabled);
            config.setMaximumSize((int) maximumSize);
            config.setExpireTime(expireAfterWrite);
            config.setExpirePolicy("expireAfterWrite");
            // 更新配置
            cacheConfigService.updateCacheConfig(cacheName, config);
        }

        return "redirect:/cache/detail/" + cacheName;
    }

    /**
     * 注册缓存管理器
     */
    @PostMapping("/register")
    public String registerCache(@RequestParam String cacheName,
                               @RequestParam(required = false, defaultValue = "10000") long maximumSize,
                               @RequestParam(required = false, defaultValue = "3600") long expireAfterWrite) {
        if (cacheConfigService.existsCacheConfig(cacheName)) {
            return "redirect:/cache/manage?error=缓存名称已存在";
        }

        // 创建配置
        CacheConfig config = new CacheConfig();
        config.setCacheName(cacheName);
        config.setInitialCapacity(100);
        config.setMaximumSize((int) maximumSize);
        config.setExpireTime(expireAfterWrite);
        config.setExpirePolicy("expireAfterWrite");
        config.setKeyStrategy("default");
        config.setOffHeap(false);
        config.setPersistent(false);
        config.setAsyncWarmup(false);
        config.setPersistentPath("./cache_data/" + cacheName);
        config.setPersistentInterval(60);

        // 存储配置
        cacheConfigService.registerCacheConfig(cacheName, config);

        // 创建缓存管理器
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(cacheMonitorService);
        cacheManager.createCache(config);
        cacheContentService.registerCacheManager(cacheName, cacheManager);

        return "redirect:/cache/manage";
    }

    // 新功能：缓存内容管理
    @GetMapping("/content/{cacheName}")
    public String cacheContent(@PathVariable String cacheName, Model model) {
        model.addAttribute("cacheName", cacheName);
        model.addAttribute("cacheKeys", cacheContentService.getCacheKeys(cacheName));
        return "cache/content";
    }

    @GetMapping("/content/{cacheName}/key/{key}")
    @ResponseBody
    public Object getCacheValue(@PathVariable String cacheName, @PathVariable String key) {
        return cacheContentService.getCacheValue(cacheName, key);
    }

    @PostMapping("/content/{cacheName}/remove/{key}")
    public String removeCacheKey(@PathVariable String cacheName, @PathVariable String key, RedirectAttributes redirectAttributes) {
        boolean success = cacheContentService.removeCacheKey(cacheName, key);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "成功删除key: " + key);
        } else {
            redirectAttributes.addFlashAttribute("error", "删除key失败: " + key);
        }
        return "redirect:/cache/content/" + cacheName;
    }

    @PostMapping("/content/{cacheName}/clear")
    public String clearCache(@PathVariable String cacheName, RedirectAttributes redirectAttributes) {
        boolean success = cacheContentService.clearCache(cacheName);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "成功清空缓存: " + cacheName);
        } else {
            redirectAttributes.addFlashAttribute("error", "清空缓存失败: " + cacheName);
        }
        return "redirect:/cache/content/" + cacheName;
    }

    // 新功能：缓存监控
    @GetMapping("/monitor")
    public String cacheMonitor(Model model) {
        model.addAttribute("cacheMetrics", cacheMonitorService.getAllCacheMetrics());
        return "cache/monitor";
    }

    @GetMapping("/monitor/{cacheName}")
    public String cacheMonitorDetail(@PathVariable String cacheName, Model model) {
        model.addAttribute("cacheName", cacheName);
        model.addAttribute("metrics", cacheMonitorService.getCacheMetrics(cacheName));
        return "cache/monitor-detail";
    }

    /**
     * 刷新缓存
     */
    @PostMapping("/refresh/{cacheName}")
    @ResponseBody
    public String refreshCache(@PathVariable String cacheName) {
        if (!cacheContentService.existsCacheManager(cacheName)) {
            return "缓存不存在";
        }

        // 持久化缓存
        CacheConfig config = cacheConfigService.getCacheConfig(cacheName);
        if (config != null && config.isPersistent()) {
            // 这里可以添加持久化逻辑
        }

        return "缓存刷新成功";
    }
}