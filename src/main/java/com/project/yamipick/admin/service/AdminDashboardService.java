package com.project.yamipick.admin.service;

import com.project.yamipick.admin.dto.DashboardDTO;
import com.project.yamipick.log.repository.BusinessLogQueryRepository;
import com.project.yamipick.user.repository.UserRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BusinessLogQueryRepository logQueryRepository;

    // 나중에 추가될 Repository들 (주석 처리)
    // private final PaymentRepository paymentRepository;
    // private final ReservationRepository reservationRepository;

    public DashboardDTO getDashboardData() {
        
        // 1. [Real] 회원 통계
        long todayJoin = userRepository.countByCreatedAt(LocalDate.now());
        long totalUser = userRepository.count();

        // 2. [Real] 방문자 차트
        List<String> visitLabels = new ArrayList<>();
        List<Long> visitData = new ArrayList<>();
        try {
            List<Tuple> stats = logQueryRepository.getDailyVisitStats();
            for (Tuple tuple : stats) {
                visitLabels.add(tuple.get(1, Integer.class) + "/" + tuple.get(2, Integer.class));
                visitData.add(tuple.get(3, Long.class));
            }
        } catch (Exception e) {}

        // 3. [Real] 인기 식당 랭킹
        List<String> rankLabels = new ArrayList<>();
        List<Long> rankData = new ArrayList<>();
        try {
            List<Tuple> topStores = logQueryRepository.getTopPopularStores();
            for (Tuple t : topStores) {
                rankLabels.add("식당 " + t.get(0, String.class)); 
                rankData.add(t.get(1, Long.class));
            }
        } catch (Exception e) {}

        // =============================================
        // [TODO] 팀원들이 기능 완성하면 여기를 진짜 코드로 교체!
        // =============================================
        long currentRevenue = 3450000; 
        long targetRevenue = 5000000;
        int reservationRate = 73;
        int noShowRate = 12;

        return DashboardDTO.builder()
                .todayJoin(todayJoin)
                .totalUser(totalUser)
                .visitLabels(visitLabels)
                .visitData(visitData)
                .rankLabels(rankLabels)
                .rankData(rankData)
                .currentRevenue(currentRevenue)
                .targetRevenue(targetRevenue)
                .reservationRate(reservationRate)
                .noShowRate(noShowRate)
                .build();
    }
}