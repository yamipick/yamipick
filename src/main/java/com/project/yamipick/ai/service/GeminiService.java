package com.project.yamipick.ai.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yamipick.ai.dto.GeminiResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();
    
    public Mono<String> generateText(String prompt) {

        String body = """
            {
              "contents": [
                {
                  "parts": [
                    { "text": "%s" }
                  ]
                }
              ]
            }
            """.formatted(prompt);

        return webClient.post()
                .uri("/v1beta/models/" + model + ":generateContent")
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)  // 🔥 요청 시점에 키 추가
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(res -> {
                    // 정상 text 추출
                    try {
                        return res.getCandidates().get(0)
                                .getContent()
                                .getParts().get(0)
                                .getText();
                    } catch (Exception e) {
                        return "딥변을 생성하는 중 문제가 발생했어요.";
                    }
                })
                .onErrorReturn("AI 응답을 가져오는 중 문제가 발생했어요.");
    }
    
    /* 사용자의 자연어 입력 → ["spicy","noodle",...] 태그 리스트로 변환 */
    public Mono<List<String>> extractTags(String userMessage) {

        String prompt = """
            다음 사용자의 문장에서 음식 추천을 위한 태그를 추출해줘.
            태그는 반드시 다음 9개 중에서만 선택해야 해:

            ["spicy", "sweet", "salty", "meat", "seafood", "soup", "noodle", "oily", "healthy"]

            - 사용자의 문맥을 잘 이해해서 복합적으로 매칭해.
            - 부정 표현("싫어", "말고", "제외")이 있으면 해당 태그는 선택하지 마.
            - 결과는 JSON 배열 형태로만 반환해. 예) ["spicy","noodle"]
            - 설명, 문장, 코드블록 없이 순수 JSON 배열만 출력해.

            사용자 입력: "%s"
            """.formatted(userMessage);

        return generateText(prompt)
                .map(text -> {
                    try {
                        // 제미나이가 ["spicy","noodle"] 형태로 준다고 가정
                        return mapper.readValue(text, new TypeReference<List<String>>() {});
                    } catch (Exception e) {
                        // 혹시 JSON 파싱이 안 되면 빈 리스트 반환
                        return Collections.<String>emptyList();
                    }
                });
    }
    
    // "이 문장이 메뉴/음식 추천 요청인지" AI가 판별
    public Mono<Boolean> detectRecommendIntent(String userMessage) {

        String prompt = """
            너는 분류기야.

            사용자의 말을 보고,
            그 말이 "음식/메뉴를 추천해 달라는 요청"인지 판별해줘.

            예를 들어 다음은 추천 요청이야.
            - "매운 거 뭐 먹을까?"
            - "오늘 저녁 메뉴 추천해줘"
            - "국물 있는 음식 뭐가 좋을까?"
            - "배고픈데 뭐 먹지"

            다음은 추천 요청이 아니야.
            - "안녕"
            - "오늘 힘들었다"
            - "너는 누구야?"
            - "날씨 어때?"

            절대 설명하지 말고,
            딱 한 단어로만 대답해.

            추천 요청이면: YES
            추천 요청이 아니면: NO

            사용자 입력: "%s"
            """.formatted(userMessage);

        return generateText(prompt)
                .map(text -> {
                    String t = text.trim().toUpperCase();
                    // "YES", "YES\n", "YES." 같은 경우까지 처리
                    if (t.startsWith("YES")) return true;
                    if (t.startsWith("NO")) return false;
                    // 혹시 이상한 답이면 일단 false
                    return false;
                })
                .onErrorReturn(false); // 오류 시에는 추천 안 하는 걸로
    }

}
