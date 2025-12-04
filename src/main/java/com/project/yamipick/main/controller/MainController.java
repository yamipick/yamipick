package com.project.yamipick.main.controller;

import com.project.yamipick.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final BannerRepository bannerRepository;

    @GetMapping("/")
    public String main(Model model) {
        // [수정] findByIsVisibleOrderByOrderNoAsc -> findByIsVisibleOrderBySeqBannerDesc
        // ERD에 orderNo가 없어서 최신순(SeqBanner Desc)으로 가져옵니다.
        // "Y"는 노출 상태인 배너만 가져오겠다는 의미입니다.
        model.addAttribute("banners", bannerRepository.findByIsVisibleOrderBySeqBannerDesc("Y"));
        
        return "main/index"; // templates/main/index.html
    }
}