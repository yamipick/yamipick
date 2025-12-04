package com.project.yamipick.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.yamipick.admin.dto.AdminUserDTO;
import com.project.yamipick.admin.service.AdminUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 회원 목록 페이지
    @GetMapping("/list")
    public String userList(
            Model model,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "seqUser", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 서비스 호출
        Page<AdminUserDTO> userPage = adminUserService.getUserList(keyword, pageable);

        model.addAttribute("list", userPage);     // 페이징된 데이터
        model.addAttribute("keyword", keyword);   // 검색어 유지
        model.addAttribute("menu", "member"); // 사이드바 활성화용

        return "admin/user/list";
    }

    // ★ [추가] 상태 변경 처리 (POST)
    // 모달창의 <form action="/admin/user/status" method="post"> 요청을 받는 곳
    @PostMapping("/status")
    public String updateStatus(
            @RequestParam Long seqUser,
            @RequestParam String status,
            @RequestParam(required = false) String reason
    ) {
        // 1. 서비스 호출해서 DB 업데이트
        adminUserService.updateUserStatus(seqUser, status, reason);
        
        // 2. 처리가 끝나면 다시 리스트 페이지로 돌아감 (새로고침 효과)
        return "redirect:/admin/user/list";
    }
}