package com.project.yamipick.waiting.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.project.yamipick.waiting.handler.WaitingWebSocketHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WaitingWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // setAllowedOrigins("*") : 보안 무시하고 접속 허용 (CORS 해결)
        registry.addHandler(handler, "/ws/waiting").setAllowedOrigins("*");
    }
}
