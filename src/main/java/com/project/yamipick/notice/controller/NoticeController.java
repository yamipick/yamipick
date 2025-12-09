package com.project.yamipick.notice.controller;

import com.project.yamipick.notice.entity.Notice;
import com.project.yamipick.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/notice") // 일반 사용자는 /admin이 안 붙습니다.
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepository;

    // 공지사항 목록
    @GetMapping("/list")
    public String list(Model model) {
        // 최신순으로 정렬해서 가져오기
        List<Notice> list = noticeRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("list", list);
        return "notice/list"; // templates/notice/list.html
    }

    // 공지사항 상세보기
    @GetMapping("/view")
    public String view(@RequestParam Long seq, Model model) {
        Notice notice = noticeRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        // 조회수 증가 (간단 구현)
        notice.increaseViewCount(); // 엔티티에 메소드 추가 필요
        noticeRepository.save(notice);

        model.addAttribute("notice", notice);
        return "notice/view"; // templates/notice/view.html
    }
}