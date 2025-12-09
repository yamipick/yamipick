package com.project.yamipick.admin.controller;

import com.project.yamipick.admin.dto.DashboardDTO;
import com.project.yamipick.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminDashboardService dashboardService;

    // 메인 진입
    @GetMapping("")
    public String index() {
        return "redirect:/admin/dashboard";
    }

    // 대시보드
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        
        // 1. 서비스에서 깔끔하게 포장된 데이터 받아오기
        DashboardDTO data = dashboardService.getDashboardData();

        // 2. 화면으로 전달
        model.addAttribute("todayJoin", data.getTodayJoin());
        model.addAttribute("totalUser", data.getTotalUser());
        
        model.addAttribute("visitLabels", data.getVisitLabels());
        model.addAttribute("visitData", data.getVisitData());
        
        model.addAttribute("rankLabels", data.getRankLabels());
        model.addAttribute("rankData", data.getRankData());
        
        model.addAttribute("currentRevenue", data.getCurrentRevenue());
        model.addAttribute("targetRevenue", data.getTargetRevenue());
        model.addAttribute("reservationRate", data.getReservationRate());
        model.addAttribute("noShowRate", data.getNoShowRate());
        model.addAttribute("noticeList", data.getNoticeList());	
        
        // 사이드바 활성화용
        model.addAttribute("menu", "dashboard");

        return "admin/dashboard";
    }
}