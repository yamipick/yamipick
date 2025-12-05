package com.project.yamipick.map.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import org.springframework.cache.annotation.Cacheable;
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

    private static final String KAKAO_REST_API_KEY = "c46ba799390d0dd28c7adc89aa77bc40"; 
    
    private static final String KAKAO_KEYWORD_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    private static final String KAKAO_CATEGORY_URL = "https://dapi.kakao.com/v2/local/search/category.json";

	/*
	 * @Cacheable(value = "restaurantSearch", key =
	 * "#query + '_' + #filterType + '_' + #x + '_' + #y + '_' + #radius + '_' + #vibe + '_' + #category + '_' + #parking + '_' + #reservable + '_' + #corkage + '_' + #price"
	 * )
	 */
    public List<RestaurantDTO> search(String query, String filterType, String x, String y, Integer radius, 
                                      String vibe, String category, 
                                      Boolean parking, Boolean reservable, Boolean corkage, String price) {
        
	        System.out.println("★ [API 직접 호출] Redis에 없어서 요청함 -> 검색어: " + query); 

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_REST_API_KEY.trim());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<RestaurantDTO> finalResultList = new ArrayList<>();

        String finalQuery = query;
        if ("맛집".equals(finalQuery)) finalQuery = "";
        if (vibe != null && !vibe.isEmpty()) {
            finalQuery = (finalQuery == null ? "" : finalQuery) + " " + vibe; 
            finalQuery = finalQuery.trim();
        }

        // [타겟 설정]
        List<String> targetCodes = new ArrayList<>();
        boolean isCafeSearch = "카페".equals(category);
        boolean isFoodSearch = !isCafeSearch && (category != null && !category.isEmpty() && !"음식 종류".equals(category));

        if (isCafeSearch) {
            targetCodes.add("CE7");
        } else if (isFoodSearch) {
            targetCodes.add("FD6");
        } else {
            // 카테고리 없으면 무조건 둘 다 검색
            targetCodes.add("FD6");
            targetCodes.add("CE7");
        }

        boolean useCategorySearch = (finalQuery == null || finalQuery.isEmpty()) && (x != null && y != null);
        String targetUrl = useCategorySearch ? KAKAO_CATEGORY_URL : KAKAO_KEYWORD_URL;

        // [황금 밸런스 3페이지]
        int r = (radius != null) ? radius : 1000;
        int pageSize = 15; 
        int pageLimit = 3; 

        if (r <= 500) pageLimit = 1; 
        if (targetCodes.size() == 1) pageLimit = 4;

        for (String code : targetCodes) {
            for (int page = 1; page <= pageLimit; page++) {
                try {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetUrl)
                            .queryParam("category_group_code", code)
                            .queryParam("size", pageSize)
                            .queryParam("page", page);

                    if (!useCategorySearch) builder.queryParam("query", finalQuery);
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
                        long id = Long.parseLong(idStr);
                        Random random = new Random(id);

                        int fakeReviewCount = random.nextInt(900) + 10;
                        double fakeRating = 3.5 + (random.nextDouble() * 1.5);
                        boolean isParking = random.nextBoolean();
                        boolean isReservable = random.nextBoolean();
                        boolean isCorkage = random.nextInt(10) < 2;
                        String[] prices = {"1만원 미만", "1만원대", "2~3만원", "3~5만원", "5~10만원", "10만원 이상"};
                        String myPrice = prices[random.nextInt(prices.length)];

                        if (Boolean.TRUE.equals(parking) && !isParking) continue;
                        if (Boolean.TRUE.equals(reservable) && !isReservable) continue;
                        if (Boolean.TRUE.equals(corkage) && !isCorkage) continue;
                        if (price != null && !price.isEmpty() && !myPrice.equals(price)) continue;

                        String name = (String) doc.get("place_name");
                        String catName = (String) doc.get("category_name");
                        String address = (String) doc.get("road_address_name");
                        if(address == null || address.isEmpty()) address = (String) doc.get("address_name");
                        
                        List<String> myTags = generateSmartTags(name, catName, address, random);
                        if (vibe != null && !vibe.isEmpty()) {
                            if (!myTags.contains(vibe)) { myTags.remove(myTags.size() - 1); myTags.add(0, vibe); }
                        }

                        RestaurantDTO dto = RestaurantDTO.builder()
                                .id(idStr).name(name).address(address).roadAddress(address)
                                .phone((String) doc.get("phone")).x((String) doc.get("x")).y((String) doc.get("y"))
                                .url((String) doc.get("place_url")).category(catName)
                                .reviewCount(fakeReviewCount).rating(Math.round(fakeRating * 10) / 10.0)
                                .tags(myTags).parking(isParking).reservable(isReservable)
                                .corkageFree(isCorkage).priceRange(myPrice).build();

                        finalResultList.add(dto);
                    }
                    if ((boolean) meta.get("is_end")) break;
                } catch (Exception e) { break; }
            }
        }

        // [정렬 로직]
        if ("reviews".equals(filterType)) {
            Collections.sort(finalResultList, Comparator.comparingInt(RestaurantDTO::getReviewCount).reversed());
        } else if ("rating".equals(filterType)) {
            Collections.sort(finalResultList, Comparator.comparingDouble(RestaurantDTO::getRating).reversed());
        } else {
            // ★ [핵심] 기본순일 때 식당/카페가 골고루 섞이도록 랜덤 셔플!
            Collections.shuffle(finalResultList);
        }

        return finalResultList;
    }

    private List<String> generateSmartTags(String name, String category, String address, Random random) {
        // (기존과 동일)
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