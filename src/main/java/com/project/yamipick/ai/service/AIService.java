package com.project.yamipick.ai.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yamipick.ai.dto.AnalysisResult;
import com.project.yamipick.ai.dto.ExtractedTags;
import com.project.yamipick.ai.dto.MenuRecommendResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIService {

    private final GeminiService gemini;
    private static final ObjectMapper mapper = new ObjectMapper();

    // 감정 키워드 목록
    private static final List<String> EMOTION_KEYWORDS = List.of(
    		"우울", "슬퍼", "슬퍼요", "기분", "기분이", "짜증", "화나", "빡쳐",
            "화나요", "스트레스", "힘들어", "지쳤어", "피곤해", "행복", "신나",
            "즐거워", "외로워", "속상해"
    );
    
    public boolean containsEmotionKeyword(String msg) {
        if (msg == null || msg.isBlank()) return false;
        return EMOTION_KEYWORDS.stream().anyMatch(msg::contains);
    }
    
    //통합 분석: 태그 + 감정 1회 호출
    public AnalysisResult analyzeTagsAndEmotion(String userMsg) {

        String prompt = """
        너는 음식과 감정 분석 전문가야. 
        문장에서 어떤 음식을 먹고 싶어하는지, 또 어떤 음식을 선호하지 않는지 맛 태그와 감정을 분석하여 아래 JSON 형태로 반환해. 맛 태그와 감정은 반드시 아래 있는 목록에 있는 것만 사용해.
        맛 태그: ["spicy","sweet","salty","meat","seafood","soup","noodle","oily","healthy"]
        감정: ["tired","stressed","sad","angry","hungry","sick","happy","neutral"]

        JSON 형식:
        {
          "positive": [...],
          "negative": [...],
          "emotion": "neutral"
        }

        문장: "%s"
        """.formatted(userMsg);

        String json = gemini.call(prompt).blockOptional().orElse("{}");

        try {
            return mapper.readValue(json, AnalysisResult.class);
        } catch (Exception e) {
            return new AnalysisResult(); // fallback
        }
    }

    //태그만 룰 기반 분석
	public ExtractedTags analyzeTagsOnly(String msg) {
	    return fallbackTagExtraction(msg.toLowerCase(), new ExtractedTags(List.of(), List.of()));
	}

	//이유 생성 (Gemini 호출 없음)
	public String generateReasonWithoutGemini(MenuRecommendResponse best,
	                                          List<String> tags,
	                                          String emotion) {
	
	    String menu = best.getMenuName();
	
	    String tagText = tags.isEmpty()
	            ? "사용자님의 취향을 잘 반영한"
	            : String.join(", ", tags) + " 취향에 맞춘";
	
	    return "%s이(가) 지금 사용자님께 잘 맞아 보여요! %s 메뉴라서 더 만족스러울 거예요."
	            .formatted(menu, tagText);
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
	            "안맵", "맵지않", "맵게말고", "덜맵", "맵기싫", "매운거말고", "안매운", "좀 안맵게", "매운건싫", "매운거싫"))
	    {
	        addOnce(neg,"spicy");
	        pos.remove("spicy");
	    }
	
	
	    // sweet
	    if (contains(msg,
	            "달달", "달콤", "단맛", "스윗", "당기는", "당기네", "달큰", "sweet"))
	        addOnce(pos,"sweet");
	
	    if (contains(msg,
	            "안달", "달지않", "단거말고", "단건싫", "단거싫", "달달한건말고", "단건별로", "단맛제외"))
	    {
	        addOnce(neg,"sweet");
	        pos.remove("sweet");
	    }
	
	
	    // salty
	    if (contains(msg,
	            "짠", "짭짤", "간세", "자극적", "강한간", "짠맛"))
	        addOnce(pos,"salty");
	
	    if (contains(msg,
	            "안짜", "싱거운", "짜지않", "싱겁게", "짜지않았으면", "짜지말고", "짠건싫", "짠거싫"))
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
	            "국물있는거말고", "국물있는건싫", "드라이", "볶음", "국물뺀"))
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
	            "안느끼", "느끼한거싫", "기름기없", "기름말고", "담백하게", "기름진건싫", "기름진거싫"))
	    {
	        addOnce(neg,"oily");
	        pos.remove("oily");
	    }
	
	
	    // healthy
	    if (contains(msg,
	            "가벼운", "담백", "건강", "샐러드", "라이트", "헬시", "healthy", "깔끔한"))
	        addOnce(pos,"healthy");
	
	    if (contains(msg,
	            "샐러드싫", "건강식말고", "담백한건말고", "가벼운건싫", "가벼운거싫", "라이트말고"))
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

	public String generateReasonWithFallback(String userMsg, MenuRecommendResponse best, List<String> tags, String emotion) {
		
		String prompt = """
	    너는 음식 추천 AI 챗봇이야.
	    사용자가 한 말: "%s"
	    추천 메뉴: "%s"
	    사용자의 조건 태그: %s
	    현재 감정: %s

	    위 정보를 바탕으로
	    친구처럼 자연스럽게 2~3문장으로 왜 이 메뉴를 추천했는지 이유를 말해줘.
	    """.formatted(
	            userMsg,
	            best.getMenuName(),
	            tags,
	            emotion
	    );
		
		try {
			String result = gemini.call(prompt).blockOptional().orElse("");
			
			if (result != null && !result.isBlank()) {
				return result;
			}
		} catch (Exception e) {
			//무조건 fallback
		}
		
		//AI 실패시
		return generateReasonWithoutGemini(best, tags, emotion);
	}
	
	public String generateChatReply(String userMsg) {

	    String prompt = """
	        너는 Yamipick 메뉴 추천 챗봇이야.
	        2~4문장으로 자연스럽고 공감 있게 대답해줘.
	        사용자가 음식 추천을 직접 요청하지 않았다면, 억지로 추천하지 마.

	        "%s"
	        """.formatted(userMsg);

	    try {
	        String raw = gemini.call(prompt).blockOptional().orElse("");
	        if (raw != null && !raw.isBlank()) return raw;
	    } catch (Exception e) {
	        // ignore and fallback
	    }

	    //쿼터/네트워크 문제 fallback
	    return "알겠어요. 지금 말해준 상황을 조금만 더 자세히 알려주면 더 잘 도와줄게요 🙂";
	}
	
}
