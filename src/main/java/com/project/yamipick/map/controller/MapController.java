package com.project.yamipick.map.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.yamipick.map.dto.NaverReviewDTO;
import com.project.yamipick.map.dto.RestaurantDTO;
import com.project.yamipick.map.service.KakaoSearchService;
import com.project.yamipick.map.service.NaverSearchService; // ★ 이 임포트가 중요합니다
import com.project.yamipick.map.service.WeatherService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MapController {

    private final KakaoSearchService kakaoSearchService;
    private final NaverSearchService naverSearchService; // ★ 형님! 이 줄이 빠져 있었습니다! 추가 완료.
    private final WeatherService weatherService;
    
    
    @Value("${yamipick.api.kakao.js-key}")
    private String kakaoJsKey;
    
    @GetMapping("/map")
    public String map(Model model, 
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
        
        if ((query == null || query.trim().isEmpty()) && (x == null || y == null)) {
            query = "강남역 맛집";
        }
        
        if (query == null) {
            query = "";
        }

        List<RestaurantDTO> list = kakaoSearchService.search(query, filter, x, y, radius, vibe, category, parking, reservable, corkage, price);      
        
        String displayQuery = query;
        if ("맛집".equals(query) || "강남역 맛집".equals(query)) { 
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
        
        String weatherMenu = weatherService.getRecommnedMenu();
        model.addAttribute("weatherMenu", weatherMenu);
        
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        
        return "search/map"; 
    }

    // ★ 네이버 블로그 리뷰 API 연결
    @GetMapping("/api/reviews")
    @ResponseBody
    public List<NaverReviewDTO> getReviews(@RequestParam("query") String query) {
        System.out.println("리뷰 검색 요청 들어옴: " + query);
        // naverSearchService가 이제 선언되었으니 정상 작동할 겁니다.
        return naverSearchService.searchBlogReviews(query);
    }
}