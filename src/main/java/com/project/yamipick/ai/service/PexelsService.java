package com.project.yamipick.ai.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class PexelsService {

	@Value("${pexels.api-key}")
	private String apiKey;
	
	//캐시 유지
	private final Map<String, String> imageCache = new ConcurrentHashMap<>();
	
	//비동기버전
    public Mono<String> getFoodImage(String englishName) {

    	if (englishName == null || englishName.isBlank()) {
    		return Mono.empty();
    	}
    	
    	//1. 캐시에 존재하면 즉시 반환
    	String cached = imageCache.get(englishName);
    	if (cached != null && !cached.isBlank()) {
    		return Mono.just(cached);
    	}

    	//WebClient 새로 생성
    	WebClient webClient = WebClient.builder()
    			.baseUrl("https://api.pexels.com/v1")
                .defaultHeader("Authorization", apiKey)
                .build();
    	
    	//2. 캐시에 없으면 API 호출
    	return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("query", englishName + " food")
                        .queryParam("per_page", 1)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.empty())
                .flatMap(res -> {
                	try {
                		List<Map<String, Object>> photos = 
                				(List<Map<String, Object>>) res.get("photos");
                		if (photos == null || photos.isEmpty()) return Mono.empty();
                		
                		Map<String, Object> src = (Map<String, Object>) photos.get(0).get("src");
                		if (src == null) Mono.empty();
                		
                		String url = (String) src.get("medium");
                		if (url == null || url.isBlank()) return Mono.empty();
                		
                		//캐시에 저장
                		imageCache.put(englishName, url);
                        return Mono.just(url);
                	
                	} catch (Exception e) {
						return Mono.empty();
					}
                });
    }
    
    //동기버전
    public String getFoodImageSync(String englishName) {

        if (englishName == null || englishName.isBlank()) {
            return null;
        }

        return getFoodImage(englishName)
                .blockOptional()
                .orElse(null);
    }


}

