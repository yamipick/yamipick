package com.project.yamipick.main.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.yamipick.banner.repository.BannerRepository;
import com.project.yamipick.log.repository.BusinessLogQueryRepository;
import com.project.yamipick.notice.repository.NoticeRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;
import com.querydsl.core.Tuple;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final BannerRepository bannerRepository;
    private final BusinessLogQueryRepository logQueryRepository;
    private final NoticeRepository noticeRepository;

    // ✅ 추가
    private final UserRepository userRepository;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails principal, Model model) {

        // ✅ 로그인한 경우: role에 따라 분기
        if (principal != null) {
            String loginId = principal.getUsername();
            User user = userRepository.findByUserId(loginId).orElse(null);

            if (user != null) {
                String role = user.getRole(); // "STORE" / "ROLE_STORE" 등 케이스 대비

                if (role != null && (role.equalsIgnoreCase("STORE") || role.contains("STORE"))) {
                    return "redirect:/store/dashboard";
                }
                if (role != null && (role.equalsIgnoreCase("ADMIN") || role.contains("ADMIN"))) {
                    return "redirect:/admin/dashboard";
                }

                // 일반 유저는 예약 메인으로 보내고 싶으면:
                // return "redirect:/reservation/main";
            }
        }

        // ====== (기존 메인 페이지 로직 그대로) ======
        model.addAttribute("bannerList", bannerRepository.findByIsVisibleOrderBySeqBannerDesc("Y"));

        List<String> topKeywords = new ArrayList<>();
        try {
            topKeywords = logQueryRepository.getTopSearchKeywords();
        } catch (Exception e) {
            topKeywords = new ArrayList<>();
        }
        model.addAttribute("topKeywords", topKeywords);

        List<Map<String, Object>> hotPlaces = new ArrayList<>();
        try {
            List<Tuple> ranks = logQueryRepository.getTopPopularStores();
            for (int i = 0; i < ranks.size(); i++) {
                Tuple t = ranks.get(i);
                String originalName = t.get(0, String.class);
                Long count = t.get(1, Long.class);

                String cleanName = originalName;
                if (cleanName != null && cleanName.endsWith(" 클릭됨")) {
                    cleanName = cleanName.replace(" 클릭됨", "");
                }
                if (cleanName != null && cleanName.endsWith(" 예약하기")) {
                    cleanName = cleanName.replace(" 예약하기", "");
                }

                Map<String, Object> map = new HashMap<>();
                map.put("rank", i + 1);
                map.put("name", cleanName);
                map.put("count", count);
                hotPlaces.add(map);
            }
        } catch (Exception e) {
        }
        model.addAttribute("hotPlaces", hotPlaces);

        model.addAttribute("noticeList", noticeRepository.findTop2ByOrderByCreatedAtDesc());

        return "main/index";
    }
}
