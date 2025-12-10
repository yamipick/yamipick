package com.project.yamipick.main.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.yamipick.banner.repository.BannerRepository;
import com.project.yamipick.log.repository.BusinessLogQueryRepository; // ★ 이거 추가
import com.querydsl.core.Tuple;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final BannerRepository bannerRepository;
    private final BusinessLogQueryRepository logQueryRepository; // ★ 주입 받기

    @GetMapping("/")
    public String index(Model model) {
        
        // 1. 배너 (기존 코드)
        model.addAttribute("bannerList", bannerRepository.findByIsVisibleOrderBySeqBannerDesc("Y"));

        // ==========================================
        // ★ [추가 1] 실시간 인기 검색어 TOP 5
        // ==========================================
        List<String> topKeywords = new ArrayList<>();
        try {
            topKeywords = logQueryRepository.getTopSearchKeywords();
        } catch (Exception e) {
            // 에러 나도 메인 페이지는 떠야 하니까 예외 처리
            topKeywords = new ArrayList<>(); 
        }
        model.addAttribute("topKeywords", topKeywords);


        // ==========================================
        // ★ [추가 2] 핫플레이스 랭킹 TOP 5
        // ==========================================
        List<Map<String, Object>> hotPlaces = new ArrayList<>();
        try {
            List<Tuple> ranks = logQueryRepository.getTopPopularStores();
            
            for (int i = 0; i < ranks.size(); i++) {
                Tuple t = ranks.get(i);
                
                // 1. 원본 데이터 가져오기 ("바나프레소 선릉점 클릭됨")
                String originalName = t.get(0, String.class); 
                Long count = t.get(1, Long.class);

                // 2. ★ [수정] " 클릭됨" 글자 제거하기
                String cleanName = originalName;
                if (cleanName != null && cleanName.endsWith(" 클릭됨")) {
                    cleanName = cleanName.replace(" 클릭됨", ""); // "바나프레소 선릉점"만 남음
                }
                // 혹시 "예약하기" 같은 다른 말도 섞여 있다면 추가로 제거
                if (cleanName != null && cleanName.endsWith(" 예약하기")) {
                    cleanName = cleanName.replace(" 예약하기", "");
                }

                Map<String, Object> map = new HashMap<>();
                map.put("rank", i + 1);
                map.put("name", cleanName); // 깔끔해진 이름 넣기
                map.put("count", count);
                hotPlaces.add(map);
            }
        } catch (Exception e) {
             // 무시 (빈 리스트로 나감)
        }
        model.addAttribute("hotPlaces", hotPlaces);

        return "main/index";
    }
}