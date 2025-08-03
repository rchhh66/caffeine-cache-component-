package com.caffeine.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 */
@Controller
public class HomeController {

    /**
     * 首页重定向到缓存管理页面
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/cache/manage";
    }
}