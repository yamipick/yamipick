package com.project.yamipick.admin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.yamipick.admin.service.AdminNoticeService;
import com.project.yamipick.notice.entity.Notice;
import com.project.yamipick.notice.service.NoticeService;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
@Slf4j
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final UserRepository userRepository;
    private final AdminNoticeService adminNoticeService;

    // 1. 관리자용 리스트
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("list", noticeService.getNoticeList());
        model.addAttribute("menu", "notice");
        return "admin/notice/list";
    }

    // 2. 관리자용 상세 보기
    @GetMapping("/view")
    public String view(@RequestParam("seq") Long seq, Model model) { // ("seq") 추가
        model.addAttribute("notice", noticeService.getNotice(seq));
        model.addAttribute("menu", "notice");
        return "admin/notice/view";
    }

    // 3. 글쓰기 폼
    @GetMapping("/write")
    public String writePage(Model model) {
        model.addAttribute("menu", "notice");
        return "admin/notice/form";
    }

    // 4. 글 등록 처리
    @PostMapping("/write")
    public String writeProcess(
            @RequestParam("title") String title,     // ("title") 추가
            @RequestParam("content") String content, // ("content") 추가
            @RequestParam(value = "file", required = false) MultipartFile file // ("file") 추가
    ) {
        String currentId = getCurrentUserId();
        User admin = userRepository.findByUserId(currentId)
                .orElseThrow(() -> new RuntimeException("로그인 정보가 올바르지 않습니다."));

        noticeService.registerNotice(title, content, admin.getSeqUser(), file);

        return "redirect:/admin/notice/list";
    }
    
 // 2. 수정 폼 이동 (GET)
    @GetMapping("/edit/{seq}")
    public String editForm(@PathVariable Long seq, Model model) {
        // 서비스에서 글 정보 가져와서 화면에 뿌림
        Notice notice = adminNoticeService.getNotice(seq); // 서비스 이름 확인 (adminNoticeService)
        model.addAttribute("notice", notice);
        return "admin/notice/form"; // form.html로 이동
    }

    // 3. 수정 처리 (POST)
    @PostMapping("/edit/{seq}")
    public String update(@PathVariable Long seq, 
                         @RequestParam("title") String title,      // 제목 따로 받기
                         @RequestParam("content") String content,  // 내용 따로 받기
                         @RequestParam(value = "file", required = false) MultipartFile file) { // 파일 따로 받기
        
        // 서비스에 수정 요청 (ID, 제목, 내용, 파일)
        adminNoticeService.updateNotice(seq, title, content, file);
        
        return "redirect:/admin/notice/list";
    }
    
    @GetMapping("/delete/{seq}")
    public String delete(@PathVariable Long seq) {
        
        // 서비스에 삭제 요청
        adminNoticeService.deleteNotice(seq);
        
        // 삭제 후 목록으로 돌아가기
        return "redirect:/admin/notice/list";
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        throw new RuntimeException("로그인이 필요합니다.");
    }
}