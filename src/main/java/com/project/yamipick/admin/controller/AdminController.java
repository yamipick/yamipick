package com.project.yamipick.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    // 1. 로그인 페이지 띄우기
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login"; // templates/admin/login.html을 찾아감
    }

    // 2. 로그인 성공 후 대시보드
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard"; // templates/admin/dashboard.html을 찾아감
    }
}