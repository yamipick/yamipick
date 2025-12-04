package com.project.yamipick.admin.controller;

import com.project.yamipick.notice.service.NoticeService;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/notice") // 관리자 공지사항 경로 통합
@RequiredArgsConstructor
@Slf4j
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final UserRepository userRepository;

    // 1. 관리자용 리스트 (수정/삭제 버튼 포함)
    @GetMapping("/list")
    public String adminList(Model model) {
        model.addAttribute("list", noticeService.getNoticeList());
        model.addAttribute("menu", "notice"); // 사이드바 활성화용
        return "admin/notice/list"; // 관리자용 테이블 디자인
    }

    // 2. 관리자용 상세 보기 (수정/삭제 버튼 포함)
    @GetMapping("/view")
    public String adminView(@RequestParam Long seq, Model model) {
        model.addAttribute("notice", noticeService.getNotice(seq));
        model.addAttribute("menu", "notice");
        return "admin/notice/view";
    }

    // 3. 글쓰기 폼
    @GetMapping("/write")
    public String writePage(Model model) {
        model.addAttribute("menu", "notice");
        return "admin/notice/write";
    }

    // 4. 글 등록 처리 (POST)
    @PostMapping("/write")
    public String writeProcess(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile file
    ) {
        String currentId = getCurrentUserId();
        User admin = userRepository.findByUserId(currentId)
                .orElseThrow(() -> new RuntimeException("로그인 정보가 올바르지 않습니다."));

        noticeService.registerNotice(title, content, admin.getSeqUser(), file);

        return "redirect:/admin/notice/list";
    }

    // [유틸] ID 추출
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        throw new RuntimeException("로그인이 필요합니다.");
    }
}