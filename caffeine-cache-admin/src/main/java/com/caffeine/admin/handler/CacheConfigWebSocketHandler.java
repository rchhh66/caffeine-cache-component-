package com.caffeine.admin.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.caffeine.admin.model.CacheConfig;

@Component
public class CacheConfigWebSocketHandler extends TextWebSocketHandler {

    // 存储所有活动的WebSocket会话
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立后，将会话存储起来
        SESSIONS.put(session.getId(), session);
        System.out.println("WebSocket连接已建立: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 连接关闭后，移除会话
        SESSIONS.remove(session.getId());
        System.out.println("WebSocket连接已关闭: " + session.getId());
    }

    /**
     * 发送缓存配置变更通知给所有客户端
     */
    public static void sendConfigChangeNotification(String cacheName, CacheConfig newConfig) {
        try {
            // 创建通知消息
            String message = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "type", "config_change",
                    "cacheName", cacheName,
                    "newConfig", newConfig
            ));

            // 向所有客户端发送消息
            for (WebSocketSession session : SESSIONS.values()) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        } catch (Exception e) {
            System.err.println("发送配置变更通知失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理来自客户端的消息
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);

        // 简单回复
        session.sendMessage(new TextMessage("已收到消息: " + payload));
    }
}