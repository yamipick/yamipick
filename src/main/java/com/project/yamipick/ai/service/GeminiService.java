package com.project.yamipick.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
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
                .bodyToMono(String.class);
    }
    
    @PostConstruct
    public void init() {
        System.out.println("⭐ Loaded model = " + model);
    }
}
