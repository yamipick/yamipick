package com.project.yamipick.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    /** 🔥 핵심: 오직 Gemini 응답 문자열만 반환한다 */
    public Mono<String> call(String prompt) {

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
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(res -> {
                    try {
                        return res.getCandidates().get(0)
                                .getContent()
                                .getParts().get(0)
                                .getText();
                    } catch (Exception e) {
                        return "";
                    }
                })
                .onErrorReturn("");
    }
}
