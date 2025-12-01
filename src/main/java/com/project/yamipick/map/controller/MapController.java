package com.project.yamipick.map.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.yamipick.map.dto.RestaurantDTO;
import com.project.yamipick.map.service.KakaoSearchService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MapController {

    private final KakaoSearchService kakaoSearchService;

    @GetMapping("/map")
    public String map(Model model, 
                      // ★ [수정 1] defaultValue 제거! (우리가 직접 제어하기 위해)
                      @RequestParam(name = "q", required = false) String query,
                      @RequestParam(name = "filter", required = false) String filter,
                      @RequestParam(name = "x", required = false) String x,
                      @RequestParam(name = "y", required = false) String y,
                      @RequestParam(name = "radius", required = false, defaultValue = "1000") Integer radius,
                      @RequestParam(name = "vibe", required = false) String vibe,
                      @RequestParam(name = "category", required = false) String category,
                      @RequestParam(name = "parking", required = false) Boolean parking,
                      @RequestParam(name = "reservable", required = false) Boolean reservable,
                      @RequestParam(name = "corkage", required = false) Boolean corkage,
                      @RequestParam(name = "price", required = false) String price
                      ) {
        
        // ★ [수정 2] 상황별 검색어 설정 로직 (스마트한 기본값)
        
        // 1. 아예 처음 접속이라서 검색어도 없고, 좌표도 없을 때 -> "강남역 맛집" (기본)
        if ((query == null || query.trim().isEmpty()) && (x == null || y == null)) {
            query = "강남역 맛집";
        }
        
        // 2. 그 외의 경우 (좌표가 있거나 필터가 있는 경우) -> null이면 빈 문자열로 변경
        if (query == null) {
            query = "";
        }

        // 서비스 호출
        List<RestaurantDTO> list = kakaoSearchService.search(query, filter, x, y, radius, vibe, category, parking, reservable, corkage, price);      
        
        // 화면 검색창에 보여줄 글자 정제 ("맛집" 이라는 단어 숨기기)
        String displayQuery = query;
        if ("맛집".equals(query) || "강남역 맛집".equals(query)) { // 강남역 맛집도 처음에 안 보이게
            displayQuery = ""; 
        }
        
        model.addAttribute("list", list);
        model.addAttribute("query", displayQuery); 
        model.addAttribute("filter", filter);
        model.addAttribute("category", category);
        model.addAttribute("vibe", vibe);
        model.addAttribute("x", x);
        model.addAttribute("y", y);
        model.addAttribute("parking", parking);
        model.addAttribute("reservable", reservable);
        model.addAttribute("corkage", corkage);
        model.addAttribute("price", price);
        
        return "search/map"; 
    }
}	