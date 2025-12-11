package com.project.yamipick.ai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.ai.dto.AIChatMessageDTO;
import com.project.yamipick.ai.dto.ChatRequest;
import com.project.yamipick.ai.dto.ChatResponse;
import com.project.yamipick.ai.dto.ExtractedTags;
import com.project.yamipick.ai.dto.MenuRecommendRequest;
import com.project.yamipick.ai.dto.MenuRecommendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final IntentDetector intentDetector;
    private final AIService ai;
    private final MenuRecommendService menuRecommendService;
    private final AIChatSessionService aiSessionService;
    private final AIChatMessageService messageService;
    
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
        
        // 1) 추천 의도 판단
        boolean isRecommend = intentDetector.isRecommendIntent(userMsg);

        ChatResponse response;
        if (isRecommend) {
        	response = handleRecommend(sessionId, userMsg);
        } else {
            response = handleChat(sessionId, userMsg);
        }
        
        //AI의 응답도 DB에 저장
        messageService.saveMessage(AIChatMessageDTO.builder()
        		.seqSession(sessionId)
        		.senderType(AIChatMessageDTO.SenderType.AI)
        		.messageText(response.getAiMessage())
        		.build());
        
        return response;
    }

    /* =======================================
     * 🔥 추천 플로우
     * ======================================= */
    private ChatResponse handleRecommend(Long sessionId, String userMsg) {

    	//세션의 과거 대화 기록 가져오기(문맥 파악)
    	List<AIChatMessageDTO> history = messageService.getMessagesBySession(sessionId);
    	
    	//이전 User 메시지들만 추출해 하나의 문자열로 합침
    	String contextHistory = history.stream()
    			.filter(m -> m.getSenderType() == AIChatMessageDTO.SenderType.USER)
    			.map(AIChatMessageDTO::getMessageText)
    			.collect(Collectors.joining(" "));
    	
    	//현재 입력 메시지까지 합치기
    	String fullContext = contextHistory + " " + userMsg;
    	    	
    	//합쳐진 전체 텍스트로 태그 분석 실행
    	ExtractedTags tags = ai.analyzeTags(fullContext);
    	
    	//감정 분석은 "현재" 기분
        String emotion = ai.analyzeEmotion(userMsg);
        
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
                    .aiMessage("말씀하신 조건(" + tags.getPositiveTags() + ")에 맞는 메뉴를 찾지 못했어요. 😭 다른 조건을 말씀해 주실래요?")
                    .timestamp(LocalDateTime.now())
                    .recommendList(List.of())
                    .build();
        }

        // BEST 메뉴에 대한 추천 이유 생성
        MenuRecommendResponse best = recommendList.get(0);
        String reply = ai.generateReason(fullContext, best, tags, emotion);

        return ChatResponse.builder()
                .seqSession(sessionId)
                .userMessage(userMsg)
                .aiMessage(reply)
                .timestamp(LocalDateTime.now())
                .recommendList(recommendList)
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
                .recommendList(List.of())
                .build();
    }
}
