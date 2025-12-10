package com.project.yamipick.ai.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yamipick.ai.dto.ExtractedTags;
import com.project.yamipick.ai.dto.MenuRecommendResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIService {

    private final GeminiService gemini;
    private static final ObjectMapper mapper = new ObjectMapper();


    /* =======================================
     * 1) 태그 분석 (AI + 룰기반 보정)
     * ======================================= */
    public ExtractedTags analyzeTags(String userMessage) {

        String prompt = """
            너는 음식 추천을 위한 태그 분석기야.
            아래 문장에서 태그를 JSON 형식으로 출력해.

            사용 가능한 태그:
            ["spicy","sweet","salty","meat","seafood","soup","noodle","oily","healthy"]

            절대 설명 없이 JSON만 반환해.

            "%s"
            """.formatted(userMessage);

        String json = gemini.call(prompt).blockOptional().orElse("");

        ExtractedTags tags;
        try {
            tags = mapper.readValue(json, ExtractedTags.class);
        } catch (Exception e) {
            tags = new ExtractedTags(List.of(), List.of(), List.of());
        }

        return fallbackTagExtraction(userMessage.toLowerCase(), tags);
    }


    /** === 룰 기반 보정 === */
    private ExtractedTags fallbackTagExtraction(String msg, ExtractedTags tags) {

        List<String> pos = new ArrayList<>(tags.safePositive());
        List<String> neg = new ArrayList<>(tags.safeNegative());
        List<String> ctx = new ArrayList<>(tags.safeContext());

        msg = msg.replace(" ", "");

        // spicy
        if (contains(msg, "매운","매콤","불맛","얼큰","spicy")) addOnce(pos,"spicy");
        if (contains(msg, "안맵","덜맵","맵지않")) addOnce(neg,"spicy");

        // sweet
        if (contains(msg,"달달","달콤","단거")) addOnce(pos,"sweet");
        if (contains(msg,"안달","달지않","단거말고")) addOnce(neg,"sweet");

        // salty
        if (contains(msg,"짠","짭짤","간세")) addOnce(pos,"salty");
        if (contains(msg,"안짜","싱거운","짜지않")) addOnce(neg,"salty");

        // meat
        if (contains(msg,"고기","삼겹","제육","닭","갈비")) addOnce(pos,"meat");
        if (contains(msg,"고기말고","고기싫")) addOnce(neg,"meat");

        // seafood
        if (contains(msg,"해물","해산물","오징어","새우","초밥","회")) addOnce(pos,"seafood");
        if (contains(msg,"해물말고","비린")) addOnce(neg,"seafood");

        // soup
        if (contains(msg,"국물","탕","찌개","라멘")) addOnce(pos,"soup");
        if (contains(msg,"국물없","드라이","볶음")) {
            addOnce(neg,"soup");
            addOnce(ctx,"dry");
        }

        // noodle
        if (contains(msg,"면","파스타","우동","라면")) addOnce(pos,"noodle");
        if (contains(msg,"면말고","면싫")) addOnce(neg,"noodle");

        // oily
        if (contains(msg,"기름","느끼","튀김","치킨","돈까스")) addOnce(pos,"oily");
        if (contains(msg,"안느끼","느끼한거싫","기름기없")) addOnce(neg,"oily");

        // healthy
        if (contains(msg,"가벼운","담백","건강","샐러드")) addOnce(pos,"healthy");
        if (contains(msg,"샐러드싫","건강식말고")) addOnce(neg,"healthy");

        // context
        if (contains(msg,"볶","구이")) addOnce(ctx,"dry");
        if (contains(msg,"밥","덮밥","볶음밥")) addOnce(ctx,"rice");

        return new ExtractedTags(pos, neg, ctx);
    }

    private boolean contains(String msg, String... ks) {
        for (String k : ks) if (msg.contains(k)) return true;
        return false;
    }

    private void addOnce(List<String> list, String s) {
        if (!list.contains(s)) list.add(s);
    }


    /* =======================================
     * 2) 감정 분석
     * ======================================= */
    public String analyzeEmotion(String userMsg) {

        String prompt = """
            너는 감정 분석기야.
	        사용자의 문장에서 감정을 하나만 선택해서 반환해.
	
	        가능한 감정:
	        ["tired","stressed","sad","angry","hungry","sick","happy","neutral"]
	
	        절대 설명하지 말고 감정 하나만 출력해.
	        "%s"
            """.formatted(userMsg);

        String result = gemini.call(prompt).blockOptional().orElse("").trim();
        return result.isBlank() ? "neutral" : result;
    }


    /* =======================================
     * 3) 추천 이유 생성
     * ======================================= */
    public String generateReason(String userMsg, MenuRecommendResponse best,
                                 ExtractedTags tags, String emotion) {

        String prompt = """
            너는 음식 추천 전문가이자 감정 케어 AI야.

            아래 정보를 바탕으로 2~3문장 자연스럽게 말해줘.
            사용자의 감정까지 고려해서 따뜻하게 추천해줘.

            사용자 입력: %s
            감정: %s
            추천 메뉴: %s
            매칭 태그: %s
            positive: %s
            negative: %s
            context: %s

            절대 DB에 없는 메뉴명을 언급하지 마.
            """.formatted(
                    userMsg, emotion, best.getMenuName(),
                    best.getMatchedTags(),
                    tags.getPositiveTags(), tags.getNegativeTags(), tags.getContextTags()
            );

        String raw = gemini.call(prompt).blockOptional().orElse("");
        return raw.isBlank() ? "추천 이유를 생성하지 못했어요." : raw;
    }


    /* =======================================
     * 4) 일반 대화 응답
     * ======================================= */
    public String generateChatReply(String userMsg) {

        String prompt = """
            너는 일상 대화를 자연스럽게 받아주는 AI야.
            2~4문장, 편안하고 공감 있게 대답해줘.
            음식 추천 의도가 아니면 추천하지 마.

            "%s"
            """.formatted(userMsg);

        String raw = gemini.call(prompt).blockOptional().orElse("");
        return raw.isBlank() ? "지금은 답변하기 어려워요 😢" : raw;
    }
    
    public String detectRecommendIntent(String userMsg) {

        String prompt = """
            너는 음식/메뉴 추천 의도를 판단하는 분류기야.

            아래 문장이 음식/메뉴 선택, 추천, 골라달라는 의미가 있다면 YES,
            아니라면 NO 로만 답해.

            절대로 설명하거나 다른 문장을 생성하지 마.

            문장: "%s"
            """.formatted(userMsg);

        String raw = gemini.call(prompt).blockOptional().orElse("");

        if (raw == null) return "";
        raw = raw.trim().toUpperCase();

        if (raw.startsWith("YES")) return "YES";
        if (raw.startsWith("NO")) return "NO";

        return ""; 
    }
    
    // AIService.java 안에 추가 (클래스 안, 다른 메서드들이랑 같은 레벨)
    public List<String> emotionToTags(String emotion) {

        if (emotion == null) return List.of();

        // 대소문자 섞여 들어와도 처리
        String e = emotion.trim().toLowerCase();

        return switch (e) {
	        case "tired"    -> List.of("healthy");
	        case "stressed" -> List.of("spicy");
	        case "sad"      -> List.of("sweet");
	        case "angry"    -> List.of("meat");
	        case "hungry"   -> List.of("noodle", "meat");
	        case "sick"     -> List.of("soup", "healthy");
	        case "happy"    -> List.of("seafood");
	        default         -> List.of();
        };
     }
}
