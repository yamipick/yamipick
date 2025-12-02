package com.project.yamipick.waiting.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WaitingWebSocketHandler extends TextWebSocketHandler {

    // 접속한 세션 관리 (Key: waitingId, Value: Session)
    private static final Map<Long, WebSocketSession> waitingSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    // 1. 연결 성공 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("🔌 [웹소켓] 클라이언트 접속됨! (Session ID: " + session.getId() + ")");
    }

    // 2. 메시지 수신 (핸들러의 핵심!)
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("📩 [웹소켓] 메시지 수신: " + payload);

        try {
            // {"waitingId": 1} 형태의 JSON을 파싱
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            
            if (data.containsKey("waitingId")) {
                Long waitingId = Long.valueOf(String.valueOf(data.get("waitingId")));
                
                // ★ 여기가 제일 중요합니다 (매핑 저장)
                waitingSessions.put(waitingId, session);
                System.out.println("✅ [웹소켓] 매핑 성공! (WaitingID " + waitingId + " <-> Session " + session.getId() + ")");
                System.out.println("   현재 접속중인 대기자 수: " + waitingSessions.size() + "명");
            } else {
                System.out.println("⚠️ [웹소켓] waitingId가 없는 메시지입니다. 매핑 실패.");
            }
        } catch (Exception e) {
            System.err.println("❌ [웹소켓] 메시지 처리 중 에러: " + e.getMessage());
        }
    }

    // 3. 개별 알림 발송 (호출, 취소 등)
    public void sendToCustomer(Long waitingId, String msg) {
        System.out.println("🚀 [웹소켓] 발송 시도 -> WaitingID: " + waitingId + ", 내용: " + msg);
        
        WebSocketSession session = waitingSessions.get(waitingId);
        
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(msg));
                System.out.println("🔔 [웹소켓] 발송 성공!");
            } catch (IOException e) {
                System.err.println("❌ [웹소켓] 발송 중 에러: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ [웹소켓] 발송 실패: 대기자(" + waitingId + ")가 현재 접속 상태가 아닙니다.");
            System.out.println("   현재 접속자 목록: " + waitingSessions.keySet());
        }
    }

    // 4. 전체 공지 발송
    public void broadcast(String msg) {
        System.out.println("📢 [웹소켓] 전체 공지 발송 시작 (대상: " + waitingSessions.size() + "명)");
        
        if (waitingSessions.isEmpty()) {
            System.out.println("⚠️ [웹소켓] 공지를 보낼 대상이 없습니다.");
            return;
        }

        waitingSessions.values().forEach(s -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage("NOTICE:" + msg));
                } catch (IOException e) {}
            }
        });
    }

    // 5. 연결 해제
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        waitingSessions.values().remove(session);
        System.out.println("🔌 [웹소켓] 연결 끊김 (Session ID: " + session.getId() + ")");
    }
}