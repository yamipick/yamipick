package com.project.yamipick.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.project.yamipick.user.dto.UserDTO;
import com.project.yamipick.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	
	// 회원가입 페이지 이동
	@GetMapping("/join")
	public String joinPage() {
		
		return "user/join";
	}
	
	// 회원가입 처리 페이지
	@PostMapping("/join")
	public String joinProcess(UserDTO dto) {
		userService.join(dto);
		
		return "redirect:/login";
	}
	
	// ★ [추가] 로그인 페이지 (여기에 추가하면 됩니다!)
    @GetMapping("/login")
    public String loginPage() {
        // templates/admin/login.html 혹은 templates/login.html 경로에 맞춰주세요
        return "admin/login"; 
    }
	
}
