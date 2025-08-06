package com.caffeine.test;

import com.caffeine.component.config.CacheConfig;
import com.caffeine.component.core.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheAdminTestApp {
    private static CaffeineCacheManager cacheManager;
    private static HttpServer server;

    public static void main(String[] args) throws Exception {
        // 创建缓存配置
        CacheConfig config = new CacheConfig();
        config.setMaximumSize(1000);
        config.setExpireAfterWrite(60); // 60秒过期

        // 创建缓存管理器
        cacheManager = new CaffeineCacheManager(config);

        // 获取缓存实例
        Cache<Object, Object> cache = cacheManager.getCache("testCache");

        // 初始化一些测试数据
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        System.out.println("初始缓存数据:");
        System.out.println("key1: " + cache.getIfPresent("key1"));
        System.out.println("key2: " + cache.getIfPresent("key2"));
        System.out.println("初始配置 - 最大容量: " + config.getMaximumSize() + ", 过期时间: " + config.getExpireAfterWrite() + "秒");

        // 启动模拟管理后台的HTTP服务器
        startAdminServer();
        System.out.println("\n管理后台服务器已启动: http://localhost:8080");
        System.out.println("请使用curl或浏览器发送POST请求到 http://localhost:8080/update 来更新缓存配置");
        System.out.println("例如: curl -X POST -d 'maximumSize=2000&expireAfterWrite=120' http://localhost:8080/update");

        // 保持程序运行，等待配置更新
        while (true) {
            Thread.sleep(1000);
            // 定期检查缓存状态
            System.out.println("\n当前缓存状态:");
            System.out.println("key1: " + cache.getIfPresent("key1"));
            System.out.println("key2: " + cache.getIfPresent("key2"));
            System.out.println("当前配置 - 最大容量: " + cacheManager.getConfig().getMaximumSize() + ", 过期时间: " + cacheManager.getConfig().getExpireAfterWrite() + "秒");
        }
    }

    private static void startAdminServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/update", new UpdateHandler());
        ExecutorService executor = Executors.newFixedThreadPool(5);
        server.setExecutor(executor);
        server.start();
    }

    static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // 读取请求体
                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // 解析请求参数
                CacheConfig newConfig = new CacheConfig();
                String[] params = requestBody.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1];
                        switch (key) {
                            case "maximumSize":
                                newConfig.setMaximumSize(Long.parseLong(value));
                                break;
                            case "expireAfterWrite":
                                newConfig.setExpireAfterWrite(Long.parseLong(value));
                                break;
                        }
                    }
                }

                // 更新缓存配置
                System.out.println("\n接收到配置更新请求:");
                System.out.println("新的最大容量: " + newConfig.getMaximumSize());
                System.out.println("新的过期时间: " + newConfig.getExpireAfterWrite() + "秒");

                cacheManager.setConfig(newConfig);

                // 发送响应
                String response = "配置更新成功\n新的最大容量: " + newConfig.getMaximumSize() + "\n新的过期时间: " + newConfig.getExpireAfterWrite() + "秒";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // 方法不允许
            }
        }
    }

    // 程序退出时关闭服务器
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server != null) {
                server.stop(0);
            }
        }));
    }
}