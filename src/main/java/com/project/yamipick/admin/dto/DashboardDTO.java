package com.project.yamipick.admin.dto;

import java.util.List;
import com.project.yamipick.notice.entity.Notice;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardDTO {
    // 1. [Real] 회원 현황
    private long todayJoin;
    private long totalUser;
    
    // ★ [추가] 탈퇴자 & 미처리 신고
    private long todayLeftUsers;   // 오늘 탈퇴
    private long pendingReports;   // 미처리 신고 건수

    // 2. [Real] 로그 통계
    private List<String> visitLabels;
    private List<Long> visitData;
    private List<String> rankLabels;
    private List<Long> rankData;
    private List<Notice> noticeList;
    
    // ★ [추가] 인기 검색어
    private List<String> topKeywords; 

    // 3. [Dummy] (나중에 팀원이 채울 영역)
    private long targetRevenue;
    private long currentRevenue;
    private int reservationRate;
    private int noShowRate;
}