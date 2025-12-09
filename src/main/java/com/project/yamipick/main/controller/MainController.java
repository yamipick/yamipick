package com.project.yamipick.main.controller;

import com.project.yamipick.banner.repository.BannerRepository;
import com.project.yamipick.log.repository.BusinessLogQueryRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final BannerRepository bannerRepository;
    private final BusinessLogQueryRepository logQueryRepository; // ★ 추가

    @GetMapping("/")
    public String index(Model model) {
        
        // 1. 배너 목록
        model.addAttribute("bannerList", bannerRepository.findByIsVisibleOrderBySeqBannerDesc("Y"));

        // 2. ★ 핫플레이스 TOP 5 가져오기 (로그 기반)
        List<Map<String, Object>> hotPlaces = new ArrayList<>();
        
        try {
            List<Tuple> topStores = logQueryRepository.getTopPopularStores();
            
            for (Tuple t : topStores) {
                String rawMsg = t.get(0, String.class); // "바나프레소 선릉점 클릭됨"
                Long count = t.get(1, Long.class);      // 1234

                // 이름만 예쁘게 자르기
                String storeName = rawMsg;
                if (storeName != null && storeName.contains(" 클릭됨")) {
                    storeName = storeName.replace(" 클릭됨", "");
                }

                // 맵에 담기 (DTO 대신 간단하게 Map 사용)
                Map<String, Object> map = new HashMap<>();
                map.put("name", storeName);
                map.put("count", count);
                hotPlaces.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("hotPlaces", hotPlaces);

        return "main/index";
    }
}