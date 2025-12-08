package com.project.yamipick.reservation.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yamipick.user.dto.UserDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입 폼
    @GetMapping("/join")
    public String joinForm(Model model) {
        UserDTO dto = new UserDTO();
        dto.setRole("CUSTOMER"); // 기본값
        model.addAttribute("user", dto);
        return "reservation/join";   // templates/reservation/join.html
    }

    // 회원가입 처리
    @PostMapping("/joinok")
    public String joinOk(@ModelAttribute("user") UserDTO dto,
                         RedirectAttributes rttr) {

        authService.signup(dto);
        rttr.addFlashAttribute("msg", "회원가입이 완료되었습니다. 로그인해주세요.");
        return "redirect:/login";
    }

    // 로그인 폼
    @GetMapping("/login")
    public String loginForm() {
        return "reservation/login";  // templates/reservation/login.html
    }
    
}
