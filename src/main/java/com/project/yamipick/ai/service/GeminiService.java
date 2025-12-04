package com.project.yamipick.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
                        return "추천 이유 생성 중 오류가 발생했어요.";
                    }
                })
                .onErrorReturn("AI 추천 이유를 가져오는 중 문제가 발생했어요.");
    }
}
