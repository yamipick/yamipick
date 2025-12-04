package com.project.yamipick.ai.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.AIChatMessageDTO;
import com.project.yamipick.ai.dto.AIChatMessageDTO.SenderType;
import com.project.yamipick.ai.dto.ChatResponse;
import com.project.yamipick.ai.dto.MenuRecommendRequest;
import com.project.yamipick.ai.dto.MenuRecommendResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiService geminiService;
    private final AIChatMessageService messageService;
    private final AIChatSessionService sessionService;
    private final MenuRecommendService menuRecommendService;

    public ChatResponse processChat(ChatRequest req) {

        Long sessionId = req.getSeqSession();

        // 1) 세션 없으면 새로 생성
        if (sessionId == null) {
        	//TODO: 로그인 후 seqUser로 변경
            sessionId = sessionService.createSession(1L);  
        }
        
        String userMessage = req.getMessage();

        // 2) 사용자 메시지 저장
        messageService.saveMessage(
                AIChatMessageDTO.builder()
                        .seqSession(sessionId)
                        .senderType(AIChatMessageDTO.SenderType.USER)
                        .messageText(userMessage)
                        .messageCreatedAt(LocalDateTime.now())
                        .build()
        );

        // 3) AI 답변 생성
        String aiText;

        if (containsRecommendIntent(userMessage)) {

            // 태그 추출
            List<String> tags = geminiService.extractTags(userMessage).block();

            // 추천 실행
            MenuRecommendRequest recommendReq = MenuRecommendRequest.builder()
                    .tags(tags)
                    .seqSession(sessionId)
                    .userInput(userMessage)
                    .build();

            List<MenuRecommendResponse> recommends =
                    menuRecommendService.recommend(recommendReq);

            // 추천 결과를 AI가 자연스럽게 설명하도록 프롬프트 생성
            String prompt = """
                너는 음식 전문가야.
                아래 추천된 DB 메뉴들만 사용해서 자연스러운 추천 문장을 만들어줘.

                메뉴 목록:
                %s

                사용자의 취향: %s

                메뉴 설명이나 이유를 부드럽고 한국어 대화체로 2~3줄 말해줘.
                DB에 없는 음식 이름은 절대 언급하지 마.
                """.formatted(
                    recommends.stream().map(MenuRecommendResponse::getMenuName).toList(),
                    tags
                );

            aiText = geminiService.generateText(prompt).block();

        } else {
            // 기본 대화
            aiText = geminiService.generateText(userMessage).block();
        }
        
        List<MenuRecommendResponse> recommends = Collections.emptyList();
        
        // 4) 이 메시지가 "추천을 원하는 의도"인지 판단
        if (detectRecommendIntent(userMessage)) {

            // 4-1) 제미나이에게 태그 추출 요청
            List<String> tags = geminiService.extractTags(userMessage).block();

            if (tags != null && !tags.isEmpty()) {

                // 4-2) 추천 요청 객체 생성
                MenuRecommendRequest recommendReq = MenuRecommendRequest.builder()
                        .tags(tags)
                        .seqSession(sessionId)    // 챗봇 추천 → 세션 저장
                        .userInput(userMessage)   // 사용자가 했던 말 그대로 저장
                        .build();

                // 4-3) 실제 추천 실행 (tblMenu 기반 + tblAIRecommend 저장)
                recommends = menuRecommendService.recommend(recommendReq);

                if (!recommends.isEmpty()) {
                    StringBuilder sb = new StringBuilder(aiText);
                    sb.append("\n\n말씀해주신 취향을 기준으로 이런 메뉴들을 추천해볼게요 👇\n");

                    for (MenuRecommendResponse r : recommends) {
                        sb.append("- ").append(r.getMenuName()).append("\n");
                    }

                    aiText = sb.toString();
                } else {
                    aiText += "\n\n말씀해주신 취향에 딱 맞는 메뉴를 찾지 못했어요. 태그를 조금 바꿔볼까요?";
                }
            } else {
                aiText += "\n\n어떤 스타일의 음식을 원하시는지 조금 더 구체적으로 말해주면 좋을 것 같아요. 예: 매운 국물, 짭짤한 면 요리";
            }
        }
        
        // 5) AI 메시지 저장
        messageService.saveMessage(
                AIChatMessageDTO.builder()
                        .seqSession(sessionId)
                        .senderType(AIChatMessageDTO.SenderType.AI)
                        .messageText(aiText)
                        .messageCreatedAt(LocalDateTime.now())
                        .build()
        );

        // 6) 응답 반환
        return ChatResponse.builder()
                .seqSession(sessionId)
                .userMessage(userMessage)
                .aiMessage(aiText)
                .timestamp(LocalDateTime.now())
                .recommends(recommends)
                .build();
    }
    
    //이 메시지가 "메뉴/음식 추천을 원하는 말"인지 판별
    public ChatResponse processChat(ChatRequest req) {

        Long sessionId = req.getSeqSession();
        if (sessionId == null) {
            sessionId = sessionService.createSession(1L);
        }

        String userMessage = req.getMessage();

        // 1) 사용자 메시지 저장
        messageService.saveMessage(
                AIChatMessageDTO.builder()
                        .seqSession(sessionId)
                        .senderType(SenderType.USER)
                        .messageText(userMessage)
                        .messageCreatedAt(LocalDateTime.now())
                        .build()
        );

        // 🔥 여기! AI에게 "추천 의도" 물어보기
        boolean wantsRecommend = geminiService.detectRecommendIntent(userMessage).blockOptional().orElse(false);

        List<MenuRecommendResponse> recommends = Collections.emptyList();
        String aiText;

        if (wantsRecommend) {
            // === 추천 모드 ===

            // 1) 태그 추출 (이미 만들어둔 extractTagsFromUserInput 대신 Gemini 기반 쓰면 거기로 교체)
            List<String> effectiveTags = geminiService.extractTags(userMessage).blockOptional().orElse(List.of());

            if (!effectiveTags.isEmpty()) {
                MenuRecommendRequest recommendReq = MenuRecommendRequest.builder()
                        .tags(effectiveTags)
                        .seqSession(sessionId)
                        .userInput(userMessage)
                        .build();

                recommends = recommendService.recommend(recommendReq); // tblMenu 기반 + tblAIRecommend에 best 1개 저장

                if (!recommends.isEmpty()) {
                    // 추천 결과 기반으로 자연스러운 설명 생성
                    MenuRecommendResponse best = recommends.get(0);

                    String prompt = """
                        너는 음식 추천 전문가야.
                        사용자의 말을 바탕으로, 아래 메뉴를 추천하는 이유를 부드럽게 설명해줘.

                        사용자 입력: %s
                        추천 메뉴: %s
                        매칭된 태그: %s

                        2~3문장 정도로 한국어 대화체로 말해줘.
                        DB에 없는 메뉴 이름은 언급하지 마.
                        """.formatted(userMessage, best.getMenuName(), best.getMatchedTags());

                    aiText = geminiService.generateText(prompt).block();
                    if (aiText == null || aiText.isBlank()) {
                        aiText = "말씀해주신 취향을 기준으로 몇 가지 메뉴를 골라봤어요.";
                    }
                } else {
                    aiText = "말씀해주신 조건에 딱 맞는 메뉴를 찾지 못했어요. 다른 스타일로 한 번 더 말해줄래요?";
                }
            } else {
                aiText = "어떤 스타일의 음식을 원하는지 좀 더 자세히 말해줄래요? 예를 들어 '매운 국물 있는 면 요리'처럼요.";
            }

        } else {
            // === 일반 대화 모드 ===
            aiText = geminiService.generateText(userMessage).block();
            if (aiText == null) {
                aiText = "죄송해요, 잠깐 문제가 생겼어요. 다시 한 번 말씀해 줄래요? 😢";
            }
        }

        // 3) AI 메시지 저장
        messageService.saveMessage(
                AIChatMessageDTO.builder()
                        .seqSession(sessionId)
                        .senderType(SenderType.AI)
                        .messageText(aiText)
                        .messageCreatedAt(LocalDateTime.now())
                        .build()
        );

        // 4) 응답 반환
        return ChatResponse.builder()
                .seqSession(sessionId)
                .userMessage(userMessage)
                .aiMessage(aiText)
                .timestamp(LocalDateTime.now())
                .recommends(recommends)
                .build();
    }

}

