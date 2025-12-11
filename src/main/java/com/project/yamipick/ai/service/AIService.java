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
            tags = new ExtractedTags(List.of(), List.of());
        }

        return fallbackTagExtraction(userMessage.toLowerCase(), tags);
    }


    /** === 룰 기반 보정 === */
    private ExtractedTags fallbackTagExtraction(String msg, ExtractedTags tags) {

        List<String> pos = new ArrayList<>(tags.safePositive());
        List<String> neg = new ArrayList<>(tags.safeNegative());

        msg = msg.replace(" ", "");

        // spicy
        if (contains(msg,
                "매운", "매콤", "얼큰", "매운맛", "불맛", "화끈", "칼칼", "spicy"))
            addOnce(pos,"spicy");

        if (contains(msg,
                "안맵", "맵지않", "맵게말고", "덜맵", "맵기싫", "매운거말고", "안매운", "좀 안맵게"))
        {
            addOnce(neg,"spicy");
            pos.remove("spicy");
        }


        // sweet
        if (contains(msg,
                "달달", "달콤", "단맛", "스윗", "당기는", "당기네", "달큰", "sweet"))
            addOnce(pos,"sweet");

        if (contains(msg,
                "안달", "달지않", "단거말고", "단건싫", "달달한건말고", "단건별로", "단맛제외"))
        {
            addOnce(neg,"sweet");
            pos.remove("sweet");
        }


        // salty
        if (contains(msg,
                "짠", "짭짤", "간세", "자극적", "강한간", "짠맛"))
            addOnce(pos,"salty");

        if (contains(msg,
                "안짜", "싱거운", "짜지않", "싱겁게", "짜지않았으면", "짜지말고"))
        {
            addOnce(neg,"salty");
            pos.remove("salty");
        }


        // meat
        if (contains(msg,
                "고기", "삼겹", "제육", "닭", "갈비", "소고기", "돼지", "육류"))
            addOnce(pos,"meat");

        if (contains(msg,
                "고기말고", "고기싫", "고기제외", "육류말고", "고기뺀", "고기안먹"))
        {
            addOnce(neg,"meat");
            pos.remove("meat");
        }


        // seafood
        if (contains(msg,
                "해물", "해산물", "오징어", "문어", "새우", "초밥", "회", "해물향", "seafood"))
            addOnce(pos,"seafood");

        if (contains(msg,
                "해물말고", "해산물싫", "비린", "비린내", "해물제외", "해물안먹"))
        {
            addOnce(neg,"seafood");
            pos.remove("seafood");
        }


        // soup
        if (contains(msg,
                "국물", "탕", "찌개", "라멘", "라면국물", "국물있는", "얼큰한국물", "진한국물"))
            addOnce(pos,"soup");

        if (contains(msg,
                "국물없", "국물말고", "국물제외", "국물싫", "국물없는", "국물안땡겨",
                "국물있는거말고", "드라이", "볶음", "국물뺀"))
        {
            addOnce(neg,"soup");
            pos.remove("soup");
        }


        // noodle
        if (contains(msg,
                "면", "파스타", "우동", "라면", "라멘", "쫄면", "국수", "noodle"))
            addOnce(pos,"noodle");

        if (contains(msg,
                "면말고", "면싫", "면은싫", "면제외", "면안먹", "면뺀"))
        {
            addOnce(neg,"noodle");
            pos.remove("noodle");
        }


        // oily
        if (contains(msg,
                "기름", "느끼", "튀김", "치킨", "돈까스", "기름진", "기름맛"))
            addOnce(pos,"oily");

        if (contains(msg,
                "안느끼", "느끼한거싫", "기름기없", "기름말고", "담백하게", "기름진건싫"))
        {
            addOnce(neg,"oily");
            pos.remove("oily");
        }


        // healthy
        if (contains(msg,
                "가벼운", "담백", "건강", "샐러드", "라이트", "헬시", "healthy", "깔끔한"))
            addOnce(pos,"healthy");

        if (contains(msg,
                "샐러드싫", "건강식말고", "담백한건말고", "가벼운건싫", "라이트말고"))
        {
            addOnce(neg,"healthy");
            pos.remove("healthy");
        }


        return new ExtractedTags(pos, neg);
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

    	try {
    		
    		String menu = best.getMenuName();;
    		String matched = String.join(",",
    							tags.getPositiveTags()==null ?
    							List.of() : tags.getPositiveTags());
    		
    		String prompt = """
    		너는 음식 추천 전문가이자 감정 케어 AI입니다.
            사용자가 "%s" 라고 말했습니다.
            추천된 메뉴는 "%s" 입니다.
            사용자가 원한 조건(태그)은 다음과 같습니다: %s.
            감정 분석 결과는 '%s' 입니다.

            위 정보를 바탕으로, 왜 이 메뉴가 잘 맞는지
            2~3문장으로 자연스럽고 부드럽게 설명해 주세요.
            절대 DB에 없는 메뉴명을 언급하지 마.
            """.formatted(
                safe(userMsg),
                safe(menu),
                safe(matched),
                safe(emotion)
            );
    		
    		String result = gemini.call(prompt).blockOptional().orElse("");
    		
    		if (result == null || result.isBlank()) {
    			return "오늘은 %s 어떠세요? 조건에 가장 잘 맞는 메뉴예요."
    					.formatted(menu);
    		}
    		
    		return result;
    	
    	} catch (Exception e) {
    		return "오늘은 %s 어떠세요? 조건에 가장 잘 맞는 메뉴예요."
    					.formatted(best.getMenuName());
    	}
    	
    }
    
    private String safe(String s) {
    	return (s == null || s.isBlank()) ? "" : s;
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
