package com.project.yamipick.ai.service;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    private static final List<String> KEYWORDS = List.of(
    		"추천", "골라", "정해줘", "먹을까", "뭐먹", "메뉴", "먹고싶다", "싫어",
    	    "아무거나", "고민하기 싫어", "하나만", "선택해줘", "먹고싶어", "배고파",
    	    "딴거", "다른", "그거말고", "말고", "또", "더", "그외", "아닌", "다른메뉴", "또추천"
    );

    public boolean isRecommendIntent(String message) {

        if (message == null || message.isBlank()) return false;

        String msg = message.toLowerCase().replace(" ", "");

        for (String k : KEYWORDS) {
            if (msg.contains(k.replace(" ", ""))) {
                System.out.println("🐾 [INTENT] 규칙기반 YES (" + k + ")");
                return true;
            }
        }

        System.out.println("🐾 [INTENT] 규칙기반 NO");
        return false;
    }
    
    private static final List<String> CHANGE_KEYWORDS = List.of(
    	    "다른거", "딴거", "또", "그거말고", "말고", "다른", "또추천"
    	);

    	public boolean isChangeIntent(String message) {
    	    if (message == null || message.isBlank()) return false;

    	    String msg = message.toLowerCase();
    	    String compressed = msg.replace(" ", "");

    	    for (String k : CHANGE_KEYWORDS) {
    	        if (msg.contains(k) || compressed.contains(k.replace(" ", ""))) {
    	            return true;
    	        }
    	    }
    	    return false;
    	}


}
