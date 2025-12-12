package com.project.yamipick.ai.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.ai.dto.AIChatMessageDTO;
import com.project.yamipick.ai.dto.AIRecommendDTO;
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
    private final AIChatMessageService messageService;
    private final AIRecommendService aiRecommendService;
    
    @Transactional
    public ChatResponse processChat(ChatRequest req) {

    	Long sessionId = req.getSeqSession();
        String userMsg = req.getMessage();

        // 세션이 없거나 잘못된 경우 → 새 세션 생성
        if (sessionId == null || !aiSessionService.exists(sessionId)) {
            sessionId = aiSessionService.createSession(1L); // 임시로 유저 1번
            req.setSeqSession(sessionId);
        }

        //사용자 메시지 먼저 DB에 저장
        messageService.saveMessage(AIChatMessageDTO.builder()
        			.seqSession(sessionId)
        			.senderType(AIChatMessageDTO.SenderType.USER)
        			.messageText(userMsg)
        			.build());
        
        //추천 의도 판단
        boolean isRecommend = intentDetector.isRecommendIntent(userMsg);

        if (!isRecommend) {
        	AIChatSession session = aiSessionService.getEntity(sessionId);
        	
        	boolean hasPrevCondition = 
        		session != null && 
        		(
        			(session.getLastPositiveTags() != null && !session.getLastPositiveTags().isBlank() || session.getLastRecommendMenuId() != null)
        		);
        	if (hasPrevCondition) {
        		isRecommend = true; //재추천으로 간주
        	}
        }
        
        ChatResponse response = isRecommend
        		? handleRecommend(sessionId, userMsg)
        		: handleChat(sessionId, userMsg);
        
        //AI의 응답도 DB에 저장
        messageService.saveMessage(AIChatMessageDTO.builder()
        		.seqSession(sessionId)
        		.senderType(AIChatMessageDTO.SenderType.AI)
        		.messageText(response.getAiMessage())
        		.build());
        
        return response;
    }

    //추천 플로우
    private ChatResponse handleRecommend(Long sessionId, String userMsg) {

    	//재추천 여부 판단
    	boolean isChange = intentDetector.isChangeIntent(userMsg);
    	
    	//세션에서 이전 태그 불러오기
    	AIChatSession session = aiSessionService.getEntity(sessionId);
    	List<String> prevPos = parseCSV(session.getLastPositiveTags());
    	List<String> prevNeg = parseCSV(session.getLastNegativeTags());
    	
    	//태그 + 감정 분석
    	String emotion = session.getLastEmotion() == null ? "neutral" : session.getLastEmotion();
    	ExtractedTags newTags;
    	
    	//재추천이면 태그 분석 X
    	if (isChange) {
    		newTags = new ExtractedTags(List.of(), List.of());
        } else {
        	//조건 변경 문장만 분석
        	boolean hasEmotionKeyword = ai.containsEmotionKeyword(userMsg);
        	
        	if (hasEmotionKeyword) {
	        	// API 1회 호출
	        	var analysis = ai.analyzeTagsAndEmotion(userMsg);
	        	newTags = new ExtractedTags(analysis.getPositive(), analysis.getNegative());
	        	emotion = analysis.getEmotion();
        	} else {
        		// AI 호출 없이 룰 기반만 수행
        		newTags = ai.analyzeTagsOnly(userMsg);
        	}
        }
    	
    	//태그 누적
    	List<String> mergedPos = mergeTags(prevPos, newTags.getPositiveTags());
    	List<String> mergedNeg = mergeTags(prevNeg, newTags.getNegativeTags());
    	
    	//negative는 positive에서 제거
    	mergedPos.removeIf(mergedNeg::contains);

        Long excludeId = isChange ? session.getLastRecommendMenuId() : null;
        List<Long> excludeIds = (excludeId == null) ? List.of() : List.of(excludeId);
        
        // 추천 요청 DTO 구성
        MenuRecommendRequest request = MenuRecommendRequest.builder()
                .seqSession(sessionId)
                .userInput(userMsg)
                .positiveTags(mergedPos)
                .negativeTags(mergedNeg)
                .emotion(emotion)
                .excludeMenuIds(excludeIds)
                .build();

        List<MenuRecommendResponse> recommendList = menuRecommendService.recommend(request);

        // 추천 실패
        if (recommendList.isEmpty()) {
            return ChatResponse.builder()
                    .seqSession(sessionId)
                    .userMessage(userMsg)
                    .aiMessage("말씀하신 조건에 맞는 메뉴를 찾지 못했어요😭 다른 조건을 말씀해 주실래요?")
                    .timestamp(LocalDateTime.now())
                    .recommendList(List.of())
                    .build();
        }

        // BEST 메뉴에 대한 추천 이유 생성
        MenuRecommendResponse best = recommendList.get(0);
        
        //세션에저장
        session.setLastPositiveTags(String.join(",", mergedPos));
        session.setLastNegativeTags(String.join(",", mergedNeg));
        session.setLastEmotion(emotion);
        session.setLastRecommendMenuId(best.getSeqMenu());
        
        String reason = ai.generateReasonWithFallback(
                userMsg,
                best,
                mergedPos,
                emotion
        );
        best.setReason(reason);

        aiRecommendService.saveRecommend(
                AIRecommendDTO.builder()
                        .seqMenu(best.getSeqMenu())
                        .userInput(userMsg)
                        .aiReason(reason)
                        .seqSession(sessionId) // 태그 추천일 때는 null이면 그대로 저장 안 함
                        .build()
        );

        return ChatResponse.builder()
                .seqSession(sessionId)
                .userMessage(userMsg)
                .aiMessage(reason)
                .timestamp(LocalDateTime.now())
                .recommendList(recommendList)
                .build();
    }
    
    private List<String> mergeTags(List<String> oldTags, List<String> newTags) {
    	List<String> merged = new ArrayList<>(oldTags);
    	if (newTags != null) {
    		for (String t : newTags) {
    			if (!merged.contains(t)) merged.add(t);
    		}
    	}
    	return merged;
    }
    
    private List<String> parseCSV(String csv) {
    	if (csv == null || csv.isBlank()) return List.of();
    	return Arrays.stream(csv.split(","))
    			.map(String::trim)
    			.filter(s -> !s.isBlank())
    			.toList();
    }
    
	//일반 대화 플로우
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
