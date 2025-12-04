package com.project.yamipick.admin.controller;

import com.project.yamipick.banner.entity.Banner;
import com.project.yamipick.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
@Slf4j
public class AdminBannerController {

    private final BannerRepository bannerRepository;

    @Value("${file.upload.path:C:/yamipick/upload/}")
    private String uploadPath;

    // 목록 페이지
    @GetMapping("/list")
    public String list(Model model) {
        // 최신순 정렬로 변경
        model.addAttribute("list", bannerRepository.findAllByOrderBySeqBannerDesc());
        model.addAttribute("menu", "banner");
        return "admin/banner/list";
    }

    // 등록 처리
    @PostMapping("/add")
    public String add(
            @RequestParam String title,
            // @RequestParam String linkUrl, (ERD에 없어서 삭제)
            // @RequestParam Integer orderNo, (ERD에 없어서 삭제)
            @RequestParam MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            return "redirect:/admin/banner/list?error=no_file";
        }

        // 파일 저장
        String originalName = file.getOriginalFilename();
        String saveName = UUID.randomUUID() + "_" + originalName;
        String savePath = uploadPath + "banner/";

        File folder = new File(savePath);
        if (!folder.exists()) folder.mkdirs();

        file.transferTo(new File(savePath + saveName));

        // DB 저장 (컬럼 줄어듦)
        Banner banner = Banner.builder()
                .title(title)
                .imgPath("/upload/banner/" + saveName)
                .isVisible("Y") // 기본 노출
                .build();

        bannerRepository.save(banner);

        return "redirect:/admin/banner/list";
    }

    // 삭제
    @PostMapping("/delete")
    public String delete(@RequestParam Long seqBanner) {
        bannerRepository.deleteById(seqBanner);
        return "redirect:/admin/banner/list";
    }
    
    // 노출 여부 토글
    @PostMapping("/toggle")
    public String toggle(@RequestParam Long seqBanner, @RequestParam String isVisible) {
        // ... (이전과 동일 로직, 엔티티 updateInfo 사용 권장)
        Banner banner = bannerRepository.findById(seqBanner).orElseThrow();
        banner.updateInfo(banner.getTitle(), isVisible);
        bannerRepository.save(banner);
        return "redirect:/admin/banner/list";
    }
}