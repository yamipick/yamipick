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

    // ★ [업데이트] 신고 처리 (상태변경 + 제재)
    @PostMapping("/process")
    public String processReport(
            @RequestParam("seqReport") Long seqReport,       // ("이름") 명시
            @RequestParam("comment") String comment,
            @RequestParam(value = "penaltyType", defaultValue = "NONE") String penaltyType,
            @RequestParam(value = "penaltyDuration", defaultValue = "0") int penaltyDuration,
            @RequestParam(value = "currentFilter", required = false, defaultValue = "ALL") String currentFilter
    ) {
        // 서비스로 제재 정보까지 함께 전달
        adminReportService.processReport(seqReport, comment, penaltyType, penaltyDuration);
        
        return "redirect:/admin/report/list?status=" + currentFilter;
    }
}