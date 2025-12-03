package com.project.yamipick.admin.controller;

import com.project.yamipick.admin.dto.AdminUserDTO;
import com.project.yamipick.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}