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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/list")
    public String userList(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword, // 이름 명시
            @PageableDefault(size = 10, sort = "seqUser", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminUserDTO> userPage = adminUserService.getUserList(keyword, pageable);

        model.addAttribute("list", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("menu", "member");

        return "admin/user/list";
    }

    @PostMapping("/status")
    public String updateStatus(
            @RequestParam("seqUser") Long seqUser, // 이름 명시
            @RequestParam("status") String status,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        adminUserService.updateUserStatus(seqUser, status, reason);
        return "redirect:/admin/user/list";
    }
}