package com.project.yamipick.map.service;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WeatherService {

	@Value("${yamipick.api.weather.key}")
    private String apiKey; 
    
    private static final double LAT = 37.5665; 
    private static final double LON = 126.9780;

    public String getRecommnedMenu() {
        try {
            String url = "https://api.openweathermap.org/data/2.5/weather";
            
            URI uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("lat", LAT)
                    .queryParam("lon", LON)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric") // ★ 섭씨 온도로 받기
                    .build().toUri();

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            
            // 1. 날씨 상태 (Rain, Clear 등)
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
            String weatherMain = (String) weatherList.get(0).get("main");
            
            // 2. 현재 온도 (Temp)
            Map<String, Object> mainMap = (Map<String, Object>) response.get("main");
            double temp = Double.parseDouble(String.valueOf(mainMap.get("temp"))); // 온도 가져오기

            log.info("현재 날씨: {}, 온도: {}°C", weatherMain, temp);

            // ★ [추천 로직] 1순위: 기상 악화 / 2순위: 온도 / 3순위: 평범
            
            // [Case 1] 비나 눈이 오면 온도 무시하고 국물/전
            if (weatherMain.equalsIgnoreCase("Rain") || weatherMain.equalsIgnoreCase("Drizzle") || weatherMain.equalsIgnoreCase("Thunderstorm")) {
                return pickRandom("파전", "막걸리", "칼국수", "짬뽕", "우동", "김치전");
            }
            if (weatherMain.equalsIgnoreCase("Snow")) {
                return pickRandom("오뎅탕", "사케", "우동", "전골", "군고구마"); // 붕어빵 제거됨
            }

            // [Case 2] 날씨는 맑거나 흐린데, 온도가 극단적일 때
            if (temp >= 30.0) {
                // 폭염
                return pickRandom("냉면", "빙수", "소바", "밀면", "콩국수"); // 아메리카노 제거됨
            } else if (temp <= -5.0) {
                // 혹한
                return pickRandom("국밥", "순대국", "김치찌개", "부대찌개", "만두전골"); // 붕어빵 제거됨
            }

            // [Case 3] 날씨도 적당하고 온도도 평범할 때 (형님 픽)
            return pickRandom("치맥", "샌드위치", "햄버거");

        } catch (Exception e) {
            log.error("날씨 정보 가져오기 실패: {}", e.getMessage());
            return "맛집"; 
        }
    }

    // 랜덤 뽑기 도우미
    private String pickRandom(String... menus) {
        List<String> menuList = Arrays.asList(menus);
        Random random = new Random();
        return menuList.get(random.nextInt(menuList.size()));
    }
}