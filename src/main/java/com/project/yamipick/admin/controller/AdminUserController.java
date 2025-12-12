package com.project.yamipick.admin.controller;

import com.project.yamipick.admin.dto.AdminUserDTO;
import com.project.yamipick.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 목록 조회
    @GetMapping("/list")
    public String userList(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "seqUser", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminUserDTO> userPage = adminUserService.getUserList(keyword, pageable);
        model.addAttribute("list", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("menu", "member");
        return "admin/user/list";
    }

    // ★ [수정] 상태 변경 (기간 duration 추가됨)
    @PostMapping("/status")
    public String updateStatus(
            @RequestParam("seqUser") Long seqUser,
            @RequestParam("status") String status,
            @RequestParam(value = "duration", defaultValue = "0") int duration, // 추가됨
            @RequestParam(value = "reason", required = false) String reason
    ) {
        // 서비스로 기간까지 같이 전달
        adminUserService.updateUserStatus(seqUser, status, duration, reason);

        return "redirect:/admin/user/list";
    }
}