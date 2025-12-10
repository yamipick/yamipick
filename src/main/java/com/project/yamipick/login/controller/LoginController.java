package com.project.yamipick.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.yamipick.login.dto.SessionUserDTO;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }

    // [API] 현재 로그인된 내 정보 확인 (프론트에서 호출용)
    @GetMapping("/api/me")
    @ResponseBody
    public ResponseEntity<?> getMyInfo(HttpSession session) {
        SessionUserDTO user = (SessionUserDTO) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(user);
    }
}