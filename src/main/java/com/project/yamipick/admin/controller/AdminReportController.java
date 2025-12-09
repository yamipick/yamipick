package com.project.yamipick.admin.controller;

import com.project.yamipick.admin.dto.AdminReportDTO;
import com.project.yamipick.admin.service.AdminReportService;
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
@RequestMapping("/admin/report")
@RequiredArgsConstructor
@Slf4j
public class AdminReportController {

    private final AdminReportService adminReportService;

    // 신고 목록 페이지
    @GetMapping("/list")
    public String reportList(
            Model model,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status, // 이름 명시
            @PageableDefault(size = 10, sort = "seqReport", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminReportDTO> reportPage = adminReportService.getReportList(status, pageable);

        model.addAttribute("list", reportPage);
        model.addAttribute("status", status); 
        model.addAttribute("menu", "report");

        return "admin/report/list";
    }

    @PostMapping("/process")
    public String processReport(
            @RequestParam("seqReport") Long seqReport,
            @RequestParam("comment") String comment,
            // HTML의 select name="penaltyType" 값 (NONE, SUSPEND_3, ...)
            @RequestParam(value = "penaltyType", defaultValue = "NONE") String penaltyType 
    ) {
        // duration은 위 서비스 스위치문에서 자동 계산하므로 굳이 안 받아도 됨 (필요시 추가)
        adminReportService.processReport(seqReport, comment, penaltyType, 0); 
        
        return "redirect:/admin/report/list";
    }
}