package com.project.yamipick.store.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.map.dto.RestaurantDTO;
import com.project.yamipick.map.service.KakaoSearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StoreSearchApiController {

    private final KakaoSearchService kakaoSearchService;

    /**
     * 매장 등록 화면에서 호출할 카카오 장소 검색 API
     * 예: GET /api/store/search?q=역삼역 맛집
     */
    @GetMapping("/api/store/search")
    public List<RestaurantDTO> searchStore(
            @RequestParam("q") String query
    ) {
        // 우리 매장 등록엔 복잡한 필터 필요 없으니까 최소값만 넣자
        // x, y, radius, vibe, category, parking, reservable, corkage, price 전부 null
        return kakaoSearchService.search(
                query,
                null,   // filterType
                null,   // x
                null,   // y
                null,   // radius
                null,   // vibe
                null,   // category
                null,   // parking
                null,   // reservable
                null,   // corkage
                null    // price
        );
    }
}

