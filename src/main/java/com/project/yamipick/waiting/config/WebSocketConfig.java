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
    	//.setAllowedOrigins("http://localhost:8080") // 실제 배포 시에는 도메인 제한 필요
    	// setAllowedOrigins("*") : // 개발 편의를 위해 모든 출처 허용 (CORS 해제)
        registry.addHandler(handler, "/ws/waiting").setAllowedOriginPatterns("*");
    }

}
