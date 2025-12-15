//package com.project.yamipick.reservation.controller;
//
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//import com.project.yamipick.reservation.auth.CustomUserDetails;
//
//@Controller
//public class LoginMainController {
//
//    /**
//     * 로그인 성공 후 첫 진입 화면
//     * SecurityConfig.defaultSuccessUrl("/") 와 연결됨
//     */
//    @GetMapping("/")
//    public String loginMain(@AuthenticationPrincipal CustomUserDetails principal) {
//
//        // 혹시 모를 방어 (로그인 안 된 경우)
//        if (principal == null) {
//            return "redirect:/login";
//        }
//
//        return "login/loginMain";
//    }
//}
