package com.project.yamipick.map.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.yamipick.map.dto.RestaurantDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoSearchService {

    private static final String KAKAO_REST_API_KEY = "c46ba799390d0dd28c7adc89aa77bc40"; // 형님 키
    private static final String KAKAO_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    // 검색 메서드 (파라미터 11개 - 컨트롤러랑 짝꿍)
    public List<RestaurantDTO> search(String query, String filterType, String x, String y, Integer radius, 
                                      String vibe, String category, 
                                      Boolean parking, Boolean reservable, Boolean corkage, String price) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_REST_API_KEY.trim());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<RestaurantDTO> finalResultList = new ArrayList<>();

        // 1. 검색어 조합 (분위기가 있으면 검색어 뒤에 붙임)
        String finalQuery = query;
        if (vibe != null && !vibe.isEmpty()) {
            finalQuery += " " + vibe; 
        }

        // 2. 카테고리 코드 결정 (식당 FD6 / 카페 CE7)
        List<String> targetCodes = new ArrayList<>();
        if ("카페".equals(category)) {
            targetCodes.add("CE7"); 
        } else if (category != null && !category.isEmpty()) {
            targetCodes.add("FD6");
        } else {
            // 전체 검색 시 식당 + 카페 둘 다
            targetCodes.add("FD6");
            targetCodes.add("CE7");
        }

        // 3. API 요청 시작
        for (String code : targetCodes) {
            int maxPage = (targetCodes.size() > 1) ? 2 : 3; // 페이지 수 조절

            for (int page = 1; page <= maxPage; page++) {
                try {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(KAKAO_SEARCH_URL)
                            .queryParam("query", finalQuery)
                            .queryParam("category_group_code", code)
                            .queryParam("size", 15)
                            .queryParam("page", page);

                    if (x != null && y != null && radius != null) {
                        builder.queryParam("x", x).queryParam("y", y).queryParam("radius", radius).queryParam("sort", "distance");
                    }

                    URI uri = builder.build().encode().toUri();
                    ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
                    
                    Map<String, Object> body = response.getBody();
                    List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
                    Map<String, Object> meta = (Map<String, Object>) body.get("meta");

                    for (Map<String, Object> doc : documents) {
                        String idStr = (String) doc.get("id");
                        
                        // ★ [하드코딩 핵심] ID를 시드로 사용해 고정된 가짜 데이터 생성
                        long id = Long.parseLong(idStr);
                        Random random = new Random(id); // ID가 같으면 항상 같은 값이 나옴

                        int fakeReviewCount = random.nextInt(900) + 10;
                        double fakeRating = 3.5 + (random.nextDouble() * 1.5);
                        
                        // 편의시설 데이터 생성 (확률 조절)
                        boolean isParking = random.nextBoolean(); // 50%
                        boolean isReservable = random.nextBoolean(); // 50%
                        boolean isCorkage = random.nextInt(10) < 2; // 20% (희귀함)

                        // 가격대 생성
                        String[] prices = {"1만원 미만", "1만원대", "2~3만원", "3~5만원", "5~10만원", "10만원 이상"};
                        String myPrice = prices[random.nextInt(prices.length)];

                        // ---------------------------------------------------------
                        // ★ [필터링 로직] 조건 안 맞으면 갖다 버림 (continue)
                        // ---------------------------------------------------------
                        if (Boolean.TRUE.equals(parking) && !isParking) continue;
                        if (Boolean.TRUE.equals(reservable) && !isReservable) continue;
                        if (Boolean.TRUE.equals(corkage) && !isCorkage) continue;
                        if (price != null && !price.isEmpty() && !myPrice.equals(price)) continue;

                        // 데이터 추출
                        String name = (String) doc.get("place_name");
                        String catName = (String) doc.get("category_name");
                        String address = (String) doc.get("road_address_name");
                        if(address == null || address.isEmpty()) address = (String) doc.get("address_name");
                        
                        // 스마트 태그 생성
                        List<String> myTags = generateSmartTags(name, catName, address, random);
                        
                        // 분위기 필터 보정
                        if (vibe != null && !vibe.isEmpty()) {
                            if (!myTags.contains(vibe)) { 
                                myTags.remove(myTags.size() - 1); 
                                myTags.add(0, vibe); 
                            }
                        }

                        // DTO 생성
                        RestaurantDTO dto = RestaurantDTO.builder()
                                .id(idStr)
                                .name(name)
                                .address(address)
                                .roadAddress(address)
                                .phone((String) doc.get("phone"))
                                .x((String) doc.get("x")) 
                                .y((String) doc.get("y")) 
                                .url((String) doc.get("place_url"))
                                .category(catName)
                                .reviewCount(fakeReviewCount)
                                .rating(Math.round(fakeRating * 10) / 10.0)
                                .tags(myTags)
                                .parking(isParking)
                                .reservable(isReservable)
                                .corkageFree(isCorkage)
                                .priceRange(myPrice)
                                .build();

                        finalResultList.add(dto);
                    }
                    if ((boolean) meta.get("is_end")) break;
                } catch (Exception e) { break; }
            }
        }

        // 정렬
        if ("reviews".equals(filterType)) {
            Collections.sort(finalResultList, Comparator.comparingInt(RestaurantDTO::getReviewCount).reversed());
        } else if ("rating".equals(filterType)) {
            Collections.sort(finalResultList, Comparator.comparingDouble(RestaurantDTO::getRating).reversed());
        }

        return finalResultList;
    }

    // 스마트 태그 생성기
    private List<String> generateSmartTags(String name, String category, String address, Random random) {
        List<String> possibleTags = new ArrayList<>();
        
        if (category.contains("카페") || category.contains("디저트")) {
            possibleTags.addAll(Arrays.asList("디저트맛집", "커피맛집", "조용한", "인스타감성", "수다떨기좋은"));
        } else if (category.contains("술집") || category.contains("포차") || category.contains("이자카야")) {
            possibleTags.addAll(Arrays.asList("회식", "안주맛집", "하이볼", "술이술술", "새벽까지"));
        } else if (category.contains("한식") || category.contains("분식") || category.contains("국밥")) {
            possibleTags.addAll(Arrays.asList("가성비", "혼밥", "해장", "집밥느낌", "든든한한끼"));
        } else if (category.contains("양식") || category.contains("레스토랑") || category.contains("파스타")) {
            possibleTags.addAll(Arrays.asList("데이트", "기념일", "와인", "스테이크", "소개팅"));
        } else if (category.contains("고기")) {
            possibleTags.addAll(Arrays.asList("회식", "구워주는", "육즙팡팡", "소주한잔", "가족외식"));
        } else {
            possibleTags.addAll(Arrays.asList("맛집탐방", "웨이팅", "친절한", "재방문필수", "현지인맛집"));
        }

        boolean isBasement = address.contains("지하") || address.contains("B1") || name.contains("지하");
        if (!isBasement) {
            if (name.contains("스카이") || name.contains("타워") || name.contains("뷰") || name.contains("루프탑") || name.contains("라운지")) {
                possibleTags.add(0, "뷰맛집");
                possibleTags.add("야경");
            }
        } else {
            possibleTags.add("아늑한");
        }
        if (name.contains("호텔") || category.contains("오마카세")) {
            possibleTags.add("고급진");
            possibleTags.add("대접하기좋은");
        }

        List<String> resultTags = new ArrayList<>();
        if (possibleTags.size() < 2) {
            possibleTags.add("핫플레이스");
            possibleTags.add("JMT");
        }
        String tag1 = possibleTags.get(0); 
        resultTags.add(tag1);
        String tag2 = "";
        for (int i = 0; i < 10; i++) { 
            tag2 = possibleTags.get(random.nextInt(possibleTags.size()));
            if (!tag2.equals(tag1)) break;
        }
        resultTags.add(tag2);
        return resultTags;
    }
}