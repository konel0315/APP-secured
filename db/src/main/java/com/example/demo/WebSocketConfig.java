package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandlerImpl webSocketHandlerImpl;

    @Autowired  // 생성자 주입으로 WebSocketHandlerImpl을 주입
    public WebSocketConfig(WebSocketHandlerImpl webSocketHandlerImpl) {
        this.webSocketHandlerImpl = webSocketHandlerImpl;
    }

    // WebSocket 핸들러 등록
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandlerImpl, "/chat")
                .setAllowedOrigins("*");  // 모든 출처에서 접근 허용
    }
}
