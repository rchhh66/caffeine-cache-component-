package com.caffeine.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 测试控制器
 */
@Controller
public class TestController {

    /**
     * 直接访问缓存管理页面
     */
    @GetMapping("/test/cache/manage")
    public String testCacheManage() {
        return "cache/manage";
    }
}