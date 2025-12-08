package com.project.yamipick.ai.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.ChatRequest;
import com.project.yamipick.ai.dto.ChatResponse;
import com.project.yamipick.ai.dto.ExtractedTags;
import com.project.yamipick.ai.dto.MenuRecommendRequest;
import com.project.yamipick.ai.dto.MenuRecommendResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final IntentDetector intentDetector;
    private final AIService ai;
    private final MenuRecommendService menuRecommendService;
    private final AIChatSessionService aiSessionService;

    public ChatResponse processChat(ChatRequest req) {

    	Long sessionId = req.getSeqSession();
        String userMsg = req.getMessage();

        // 세션이 없거나 잘못된 경우 → 새 세션 생성
        if (sessionId == null || !aiSessionService.exists(sessionId)) {
            sessionId = aiSessionService.createSession(1L); // 임시로 유저 1번
            req.setSeqSession(sessionId);
        }

        // 1) 추천 의도 판단
        boolean isRecommend = intentDetector.isRecommendIntent(userMsg);

        if (isRecommend) {
            return handleRecommend(sessionId, userMsg);
        } else {
            return handleChat(sessionId, userMsg);
        }
    }

    /* =======================================
     * 🔥 추천 플로우
     * ======================================= */
    private ChatResponse handleRecommend(Long sessionId, String userMsg) {

        // 감정 분석 + 태그 분석
        String emotion = ai.analyzeEmotion(userMsg);
        ExtractedTags tags = ai.analyzeTags(userMsg);

        // 추천 요청 DTO 구성
        MenuRecommendRequest request = MenuRecommendRequest.builder()
                .seqSession(sessionId)
                .userInput(userMsg)
                .positiveTags(tags.getPositiveTags())
                .negativeTags(tags.getNegativeTags())
                .contextTags(tags.getContextTags())
                .emotion(emotion)
                .build();

        // TOP 3 메뉴 추천
        List<MenuRecommendResponse> recommends = menuRecommendService.recommend(request);

        // 추천 실패
        if (recommends.isEmpty()) {
            return ChatResponse.builder()
                    .seqSession(sessionId)
                    .userMessage(userMsg)
                    .aiMessage("말씀하신 조건에 맞는 메뉴를 찾지 못했어요. 조금 다르게 말해볼까요?")
                    .timestamp(LocalDateTime.now())
                    .recommends(List.of())
                    .build();
        }

        // BEST 메뉴에 대한 추천 이유 생성
        MenuRecommendResponse best = recommends.get(0);
        String reply = ai.generateReason(userMsg, best, tags, emotion);

        return ChatResponse.builder()
                .seqSession(sessionId)
                .userMessage(userMsg)
                .aiMessage(reply)
                .timestamp(LocalDateTime.now())
                .recommends(recommends)
                .build();
    }

    /* =======================================
     * ✨ 일반 대화 플로우
     * ======================================= */
    private ChatResponse handleChat(Long sessionId, String userMsg) {

        String reply = ai.generateChatReply(userMsg);

        return ChatResponse.builder()
                .seqSession(sessionId)
                .userMessage(userMsg)
                .aiMessage(reply)
                .timestamp(LocalDateTime.now())
                .recommends(List.of())
                .build();
    }
}
