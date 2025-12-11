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

//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//    	//.setAllowedOrigins("http://localhost:8080") // 실제 배포 시에는 도메인 제한 필요
//    	// setAllowedOrigins("*") : // 개발 편의를 위해 모든 출처 허용 (CORS 해제)
//        registry.addHandler(handler, "/ws/blocked-waiting").setAllowedOriginPatterns("*");
//    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 1. 콘솔에 이 로그가 찍히는지 확인해주세요!!
        System.out.println(">>> 웹소켓 설정 적용됨! 허용 도메인: http://google.com"); 

        // 2. 도메인을 말도 안 되는 주소로 바꿔보세요.
        registry.addHandler(handler, "/ws/waiting")
                .setAllowedOrigins("*"); 
    }
}
