package com.project.yamipick.ai.service;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    private final AIService ai;

    public IntentDetector(AIService ai) {
        this.ai = ai;
    }

    private static final List<String> KEYWORDS = List.of(
    		"추천", "골라", "정해줘", "먹을까", "뭐먹", "메뉴",
    	    "아무거나", "고민하기 싫어", "하나만", "선택해줘", "먹고싶어", "배고파"
    );

    public boolean isRecommendIntent(String message) {

        if (message == null || message.isBlank()) return false;

        String msg = message.toLowerCase();
        String compressed = msg.replace(" ", "");

        // 1) 규칙 기반 체크
        for (String k : KEYWORDS) {
            if (msg.contains(k) || compressed.contains(k.replace(" ", ""))) {
                System.out.println("🐾 [INTENT] 규칙기반 YES (" + k + ")");
                return true;
            }
        }

        // 2) AI 보조 판단
        String aiResult = ai.detectRecommendIntent(message);

        if (aiResult == null || aiResult.isBlank()) {
            System.out.println("🐾 [INTENT] AI 빈값 → YES로 Fallback");
            return true;
        }

        boolean finalResult = aiResult.trim().equalsIgnoreCase("YES");
        System.out.println("🐾 [INTENT] 최종 결과 = " + finalResult);

        return finalResult;
    }

}
