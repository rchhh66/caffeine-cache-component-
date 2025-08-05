package com.caffeine.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.caffeine.admin.handler.CacheConfigWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket端点，允许所有 origins 访问
        registry.addHandler(new CacheConfigWebSocketHandler(), "/ws/cache-config")
                .setAllowedOrigins("*");
    }
}