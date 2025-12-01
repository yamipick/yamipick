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

    // 접속한 세션 관리 (Key: waitingId)
    private static final Map<Long, WebSocketSession> waitingSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
            if (data.containsKey("waitingId")) {
                Long id = Long.valueOf(String.valueOf(data.get("waitingId")));
                waitingSessions.put(id, session);
                System.out.println("🔌 웹소켓 연결됨: WaitingID=" + id);
            }
        } catch (Exception e) {
            System.err.println("메시지 처리 중 오류: " + e.getMessage());
        }
    }

    // 개별 알림 (호출/입장 등)
    public void sendToCustomer(Long waitingId, String msg) {
        WebSocketSession session = waitingSessions.get(waitingId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(msg));
            } catch (IOException e) {}
        }
    }

    // 전체 공지
    public void broadcast(String msg) {
        waitingSessions.values().forEach(s -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage("NOTICE:" + msg));
                } catch (IOException e) {}
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        waitingSessions.values().remove(session);
    }
}