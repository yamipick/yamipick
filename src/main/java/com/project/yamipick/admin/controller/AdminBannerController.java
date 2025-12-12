package com.project.yamipick.admin.controller;

import com.project.yamipick.banner.entity.Banner;
import com.project.yamipick.banner.repository.BannerRepository;
import com.project.yamipick.aws.S3Uploader; // ★ 아까 만든 Uploader import 확인!
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
@Slf4j
public class AdminBannerController {

    private final BannerRepository bannerRepository;
    private final S3Uploader s3Uploader; // ★ 로컬 경로(String uploadPath) 대신 이거 씀!

    // 목록 페이지
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("list", bannerRepository.findAllByOrderBySeqBannerDesc());
        model.addAttribute("menu", "banner");
        return "admin/banner/list";
    }

    // 등록 처리 (S3 버전)
    @PostMapping("/add")
    public String add(
            @RequestParam String title,
            @RequestParam MultipartFile file
    ) { // throws IOException 제거 (try-catch로 잡음)

        if (file.isEmpty()) {
            return "redirect:/admin/banner/list?error=no_file";
        }

        try {
            // ★ 핵심 변경: 로컬 저장 로직 삭제 -> S3Uploader 호출
            // "banner"는 S3 버킷 안에 생길 폴더 이름입니다.
            String uploadedUrl = s3Uploader.upload(file, "banner");
            
            log.info("S3 업로드 성공: {}", uploadedUrl);

            // DB 저장 (S3 URL 그대로 저장)
            Banner banner = Banner.builder()
                    .title(title)
                    .imgPath(uploadedUrl) // 예: https://yamipick.s3.../banner/abc.jpg
                    .isVisible("Y")
                    .build();

            bannerRepository.save(banner);

        } catch (IOException e) {
            log.error("배너 이미지 업로드 실패", e);
            return "redirect:/admin/banner/list?error=upload_fail";
        }

        return "redirect:/admin/banner/list";
    }

    // 삭제
    @PostMapping("/delete")
    public String delete(@RequestParam Long seqBanner) {
        // 심화: 여기서 s3Uploader.delete(imgPath) 를 호출해서 S3 파일도 지워주면 베스트!
        // 일단은 DB만 지워도 무방합니다.
        bannerRepository.deleteById(seqBanner);
        return "redirect:/admin/banner/list";
    }
    
    // 노출 여부 토글 (기존 유지)
    @PostMapping("/toggle")
    public String toggle(@RequestParam Long seqBanner, @RequestParam String isVisible) {
        Banner banner = bannerRepository.findById(seqBanner).orElseThrow();
        banner.updateInfo(banner.getTitle(), isVisible);
        bannerRepository.save(banner);
        return "redirect:/admin/banner/list";
    }
}