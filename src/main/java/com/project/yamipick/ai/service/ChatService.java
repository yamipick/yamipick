package com.project.yamipick.ai.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.ChatRequest;
import com.project.yamipick.ai.dto.ChatResponse;
import com.project.yamipick.ai.dto.ExtractedTags;
import com.project.yamipick.ai.dto.MenuRecommendRequest;
import com.project.yamipick.ai.dto.MenuRecommendResponse;
import com.project.yamipick.ai.entity.AIChatSession;

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

    	//세션 불러오기
    	AIChatSession session = aiSessionService.getEntity(sessionId);
    	
    	//이전 세션 태그 불러오기
    	List<String> prevPos = parseCSV(session.getLastPositiveTags());
    	List<String> prevNeg = parseCSV(session.getLastNegativeTags());
    	String prevEmotion = session.getLastEmotion();
    	
        //현재 메시지가 재추천이면 태그 분석하지 않음
        if (isChangeIntent(userMsg)) {
        	ExtractedTags tags = new ExtractedTags(prevPos, prevNeg);
        	return recommendAndRespond(sessionId, userMsg, tags, prevEmotion);
        }
        
        //신규 태그 분석
        ExtractedTags newTags = ai.analyzeTags(userMsg);
        String emotion = ai.analyzeEmotion(userMsg);
        
        //감정 -> 태그 변환 후 positiveTags에 추가
        List<String> emotionTags = ai.emotionToTags(emotion);
        List<String> newPosWithEmotion = mergeTags(newTags.getPositiveTags(), emotionTags);
        
        //이전 조건 + 새 조건 누적 저장
        List<String> mergedPos = mergeTags(prevPos, newPosWithEmotion);
        List<String> mergedNeg = mergeTags(prevNeg, newTags.getNegativeTags());

        //세션에 태그 저장
        aiSessionService.updateTags(sessionId, mergedPos, mergedNeg, emotion);
        
        ExtractedTags finalTags = new ExtractedTags(mergedPos, mergedNeg);
        
        //추천 및 응답 생성
        return recommendAndRespond(sessionId, userMsg, finalTags, emotion);
    }
    
    private ChatResponse recommendAndRespond(Long sessionId, String userMsg, ExtractedTags tags, String emotion) {
    	
        
        // 추천 요청 DTO 구성
        MenuRecommendRequest request = MenuRecommendRequest.builder()
                .seqSession(sessionId)
                .userInput(userMsg)
                .positiveTags(tags.getPositiveTags())
                .negativeTags(tags.getNegativeTags())
                .emotion(emotion)
                .build();

        List<MenuRecommendResponse> recommendList = menuRecommendService.recommend(request);

        // 추천 실패
        if (recommendList.isEmpty()) {
            return ChatResponse.builder()
                    .seqSession(sessionId)
                    .userMessage(userMsg)
                    .aiMessage("말씀하신 조건에 맞는 메뉴를 찾지 못했어요. 조금 다르게 말해볼까요?")
                    .timestamp(LocalDateTime.now())
                    .recommendList(List.of())
                    .build();
        }

        // BEST 메뉴에 대한 추천 이유 생성
        String reply = ai.generateReason(userMsg, recommendList.get(0), tags, emotion);

        return ChatResponse.builder()
                .seqSession(sessionId)
                .userMessage(userMsg)
                .aiMessage(reply)
                .timestamp(LocalDateTime.now())
                .recommendList(recommendList)
                .build();
    }
    
	private List<String> mergeTags(List<String> oldTags, List<String> newTags) {
	    List<String> result = new ArrayList<>(oldTags);
	    if (newTags != null) result.addAll(newTags);
	    return result.stream().distinct().toList();
	}

    private boolean isChangeIntent(String msg) {
    	msg = msg.toLowerCase();
        return msg.contains("딴") ||
        		msg.contains("다른") ||
               msg.contains("말고") ||
               msg.contains("아닌") ||
               msg.contains("그건싫") ||
               msg.contains("별로") ||
               msg.contains("바꿔") ||
               msg.contains("또추천");
	}
    
    private List<String> parseCSV(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
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
                .recommendList(List.of())
                .build();
    }
}
