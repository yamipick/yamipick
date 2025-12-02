package com.project.yamipick.notice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.yamipick.notice.service.NoticeService;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;
    private final UserRepository userRepository;
    
    // 사용자 영역 (Read Only)
    @GetMapping("/notice/list")
    public String userList(Model model) {
        // 여기선 '공개된 글'만 가져오는 로직이 들어가면 더 좋음
        model.addAttribute("list", noticeService.getNoticeList());
        return "notice/list"; // 사용자용 예쁜 디자인
    }

    @GetMapping("/notice/view")
    public String userView(@RequestParam Long seq, Model model) {
        model.addAttribute("notice", noticeService.getNotice(seq));
        return "notice/view";
    }
    
 
    // 관리자 영역 (Create, Update, Delete)
    
    // 1. 관리자용 리스트 (수정/삭제 버튼 포함)
    @GetMapping("/admin/notice/list")
    public String adminList(Model model) {
        model.addAttribute("list", noticeService.getNoticeList());
        return "admin/notice/list"; // 관리자용 테이블 디자인
    }

    // 2. 관리자용 상세 보기 (수정/삭제 버튼 포함)
    @GetMapping("/admin/notice/view")
    public String adminView(@RequestParam Long seq, Model model) {
        model.addAttribute("notice", noticeService.getNotice(seq));
        return "admin/notice/view"; 
    }

    // 3. 글쓰기 폼
    @GetMapping("/admin/notice/write")
    public String writePage() {
        return "admin/notice/write";
    }

    // 4. 글 등록 처리 (POST)
    @PostMapping("/admin/notice/write") // ★ 여기 수정했습니다!
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