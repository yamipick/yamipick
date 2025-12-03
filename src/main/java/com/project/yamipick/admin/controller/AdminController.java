package com.project.yamipick.admin.controller;

import com.project.yamipick.log.repository.BusinessLogQueryRepository;
import com.project.yamipick.user.repository.UserRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin") // 이 클래스의 모든 주소 앞에 /admin이 붙음
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final BusinessLogQueryRepository logQueryRepository;

    // 1. [진입점] 그냥 /admin만 쳤을 때의 처리 (이게 없으면 404 뜸!)
    @GetMapping("") 
    public String adminHome() {
        // 멍하니 빈 화면 보여주지 말고, 바로 대시보드로 토스합니다.
        return "redirect:/admin/dashboard";
    }

    // 2. [대시보드] 관리자 페이지의 "얼굴" (통계 데이터 로딩)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("관리자 대시보드 진입");

        // (1) 오늘 가입한 신규 회원 수 (Repository에 countByCreatedAt 추가 필요)
        long newUsers = userRepository.countByCreatedAt(LocalDate.now());
        
        // (2) 전체 회원 수
        long totalUsers = userRepository.count();

        // (3) 최근 방문자 통계 (그래프용 데이터)
        // 지난번에 알려드린 QueryDSL 메서드가 있다고 가정
        // List<Tuple> stats = logQueryRepository.getDailyVisitStats(); 
        // model.addAttribute("stats", stats);

        // 화면에 뿌릴 데이터 담기
        model.addAttribute("newUsers", newUsers);
        model.addAttribute("totalUsers", totalUsers);
        
        // 사이드바에서 '대시보드' 메뉴에 불 들어오게 하기 위한 값
        model.addAttribute("menu", "dashboard");

        return "admin/dashboard"; // templates/admin/dashboard.html
    }
}