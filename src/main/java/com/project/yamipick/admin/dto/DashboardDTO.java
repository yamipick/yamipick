package com.project.yamipick.admin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardDTO {
    // 1. [Real] 회원 현황 (이미 구현됨)
    private long todayJoin;
    private long totalUser;

    // 2. [Real] 로그 통계 (이미 구현됨)
    private List<String> visitLabels;
    private List<Long> visitData;
    private List<String> rankLabels;
    private List<Long> rankData;

    // 3. [Dummy] 나중에 팀원이 채워줄 데이터 (예약, 결제 등)
    // 이 변수명들을 보고 팀원들이 "아, 이 데이터를 줘야 하는구나" 하고 알게 됩니다.
    private long targetRevenue;     // 목표 매출
    private long currentRevenue;    // 현재 매출
    private int reservationRate;    // 예약 이행률
    private int noShowRate;         // 노쇼 비율
}