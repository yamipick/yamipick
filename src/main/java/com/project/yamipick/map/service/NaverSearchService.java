package com.project.yamipick.map.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.yamipick.map.dto.NaverReviewDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverSearchService {

    // ★ 형님! 아까 발급받은 키를 여기에 붙여넣으세요!
    private static final String NAVER_CLIENT_ID = "W375NhIg2kLnKvmC2Ps8"; 
    private static final String NAVER_CLIENT_SECRET = "NL63uvVRAl";

    private static final String NAVER_SEARCH_URL = "https://openapi.naver.com/v1/search/blog.json";

    public List<NaverReviewDTO> searchBlogReviews(String query) {
        // 검색어 뒤에 "맛집"이나 "후기"를 붙여서 정확도 높임
        String text = query + " 맛집 내돈내산"; 
        
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(NAVER_SEARCH_URL)
                    .queryParam("query", text)
                    .queryParam("display", 3)  // 3개만 가져오기
                    .queryParam("start", 1)
                    .queryParam("sort", "sim") // 정확도순
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            RequestEntity<Void> req = RequestEntity
                    .get(uri)
                    .header("X-Naver-Client-Id", NAVER_CLIENT_ID)
                    .header("X-Naver-Client-Secret", NAVER_CLIENT_SECRET)
                    .build();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(req, Map.class);
            
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            
            List<NaverReviewDTO> resultList = new ArrayList<>();
            
            for (Map<String, Object> item : items) {
                String title = (String) item.get("title");
                String link = (String) item.get("link");
                String desc = (String) item.get("description");
                String blogger = (String) item.get("bloggername");
                String date = (String) item.get("postdate");

                // 네이버가 주는 <b>태그 제거 (깔끔하게 보이려고)
                title = title.replaceAll("<b>", "").replaceAll("</b>", "");
                desc = desc.replaceAll("<b>", "").replaceAll("</b>", "");

                NaverReviewDTO dto = NaverReviewDTO.builder()
                        .title(title)
                        .link(link)
                        .description(desc)
                        .bloggername(blogger)
                        .postdate(formatDate(date))
                        .build();
                
                resultList.add(dto);
            }
            
            return resultList;
            
        } catch (Exception e) {
            log.error("네이버 검색 실패: {}", e.getMessage());
            return new ArrayList<>(); // 에러나면 빈 리스트 반환 (서버 안 죽게)
        }
    }
    
    // 날짜 예쁘게 변환 (20231203 -> 2023.12.03)
    private String formatDate(String date) {
        if(date == null || date.length() != 8) return date;
        return date.substring(0, 4) + "." + date.substring(4, 6) + "." + date.substring(6, 8);
    }
}