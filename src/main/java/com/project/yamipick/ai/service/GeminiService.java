package com.project.yamipick.ai.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yamipick.ai.dto.GeminiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
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

    public Mono<String> call(String prompt) {
    	
    	try {
            // [수정] Map과 ObjectMapper를 사용해 안전한 JSON 생성 (줄바꿈 자동 처리)
            Map<String, Object> requestMap = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                )
            );

	        String body = mapper.writeValueAsString(requestMap);
	
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
	                    	log.error("Gemini 응답 파싱 실패. 응답 내용: {}", res);
	                        return "";
	                    }
	                })
	                .doOnError(e -> {
	                    log.error("Gemini API 호출 중 치명적 에러 발생!");
	                    log.error("에러 메시지: {}", e.getMessage());
	
	                    if (e instanceof WebClientResponseException) {
	                        WebClientResponseException we = (WebClientResponseException) e;
	                        log.error("HTTP 상태 코드: {}", we.getStatusCode());
	                        log.error("구글 응답 본문: {}", we.getResponseBodyAsString()); // 여기가 진짜 원인
	                    } else {
	                        log.error("예외 스택 트레이스:", e);
	                    }
	                })
	                .onErrorReturn("");
        
    	} catch (Exception e) {
    		log.error("JSON 생성 실패", e);
    		return Mono.just("");
    	}
    }
}
