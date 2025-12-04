package com.project.yamipick.notice.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.yamipick.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;
    
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
    
}