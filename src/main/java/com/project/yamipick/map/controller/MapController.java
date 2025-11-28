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
                      @RequestParam(name = "q", defaultValue = "강남역 맛집") String query,
                      @RequestParam(name = "filter", required = false) String filter,
                      @RequestParam(name = "x", required = false) String x,
                      @RequestParam(name = "y", required = false) String y,
                      @RequestParam(name = "radius", required = false, defaultValue = "1000") Integer radius,
                      @RequestParam(name = "vibe", required = false) String vibe,       // ★ 추가됨
                      @RequestParam(name = "category", required = false) String category, // ★ 추가됨
                      @RequestParam(name = "parking", required = false) Boolean parking,
                      @RequestParam(name = "reservable", required = false) Boolean reservable,
                      @RequestParam(name = "corkage", required = false) Boolean corkage,
                      @RequestParam(name = "price", required = false) String price
                      ) {
        
        // ★ [핵심 수정] 서비스에 vibe랑 category까지 총 7개를 순서대로 넘겨야 합니다!
    	List<RestaurantDTO> list = kakaoSearchService.search(query, filter, x, y, radius, vibe, category, parking, reservable, corkage, price);        
        model.addAttribute("list", list);
        model.addAttribute("query", query);
        model.addAttribute("filter", filter);
        model.addAttribute("category", category); // 화면 유지용
        model.addAttribute("vibe", vibe);         // 화면 유지용
        model.addAttribute("x", x);               // 지도 중심 유지용
        model.addAttribute("y", y);               // 지도 중심 유지용
        model.addAttribute("parking", parking);
        model.addAttribute("reservable", reservable);
        model.addAttribute("corkage", corkage);
        model.addAttribute("price", price);
        return "search/map"; 
    }
}