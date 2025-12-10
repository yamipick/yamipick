package com.project.yamipick.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime; // ★ 시간 계산용 임포트 필수
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.admin.dto.DashboardDTO;
import com.project.yamipick.log.entity.BusinessLog;
import com.project.yamipick.log.repository.BusinessLogQueryRepository;
import com.project.yamipick.log.repository.BusinessLogRepository;
import com.project.yamipick.notice.entity.Notice;
import com.project.yamipick.notice.repository.NoticeRepository;
import com.project.yamipick.report.repository.ReportRepository; // ★ 신고 리포지토리 추가
import com.project.yamipick.user.repository.UserRepository;
import com.querydsl.core.Tuple;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BusinessLogQueryRepository logQueryRepository;
    private final NoticeRepository noticeRepository;
    private final BusinessLogRepository logRepository;
    
    // ★ [추가] 신고 관리를 위해 주입 필요
    private final ReportRepository reportRepository; 

    // 나중에 추가될 Repository들 (주석 처리)
    // private final PaymentRepository paymentRepository;
    // private final ReservationRepository reservationRepository;

    // @Cacheable(value = "dashboardData") // 실시간 데이터 중요하면 캐시 끄는 게 좋음
    public DashboardDTO getDashboardData() {
        
        // ★ [핵심 수정] "오늘"의 기준을 오늘 0시 0분 0초로 잡음
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // 1. [Real] 회원 통계 (시간 문제 해결됨)
        // UserRepository에 countByCreatedAtAfter 메소드가 있어야 함
        long todayJoin = userRepository.countByCreatedAtAfter(startOfToday); 
        long totalUser = userRepository.count();
        
        // ★ [추가] 탈퇴 회원 & 미처리 신고
        long todayLeft = userRepository.countByStatusUser("WITHDRAWN"); // (일단 전체 탈퇴자 수)
        long pendingReports = reportRepository.countByStatus("PENDING"); // 처리 안 된 신고

        // 2. [Real] 인기 검색어 (로그에서 가져오기)
        List<String> topKeywords = new ArrayList<>();
        try {
            // BusinessLogQueryRepository에 해당 메소드 구현 필요
            topKeywords = logQueryRepository.getTopSearchKeywords();
        } catch (Exception e) {
            // 에러 나도 빈 리스트로 처리해서 대시보드는 뜨게 함
        }

        // 3. [Real] 방문자 차트
        List<String> visitLabels = new ArrayList<>();
        List<Long> visitData = new ArrayList<>();
        try {
            List<Tuple> stats = logQueryRepository.getDailyVisitStats();
            for (Tuple tuple : stats) {
                visitLabels.add(tuple.get(1, Integer.class) + "/" + tuple.get(2, Integer.class));
                visitData.add(tuple.get(3, Long.class));
            }
        } catch (Exception e) {}

        // 4. [Real] 인기 식당 랭킹
        List<String> rankLabels = new ArrayList<>();
        List<Long> rankData = new ArrayList<>();
        try {
            List<Tuple> topStores = logQueryRepository.getTopPopularStores();
            
            for (Tuple t : topStores) {
                String rawMessage = t.get(0, String.class);
                Long count = t.get(1, Long.class);

                // " 클릭됨" 같은 불필요한 텍스트 제거
                String storeName = rawMessage;
                if (storeName != null && storeName.contains(" 클릭됨")) {
                    storeName = storeName.replace(" 클릭됨", "");
                }
                
                rankLabels.add(storeName);
                rankData.add(count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 5. [Real] 최신 공지사항
        List<Notice> noticeList = noticeRepository.findTop5ByOrderByCreatedAtDesc();
        
        // 6. [Dummy] 예약 및 매출 (팀원 구현 대기)
        int reservationRate = 0;
        try {
             reservationRate = logQueryRepository.getReservationRate();
        } catch(Exception e) {}
        
        List<BusinessLog> recentLogs = logRepository.findTop10ByOrderByCreatedAtDesc();

        long currentRevenue = 3450000; 
        long targetRevenue = 5000000;
        int noShowRate = 12;

        return DashboardDTO.builder()
                .todayJoin(todayJoin)       // 오늘 가입 (수정됨)
                .totalUser(totalUser)
                .todayLeftUsers(todayLeft)     // ★ 추가됨
                .pendingReports(pendingReports) // ★ 추가됨
                .topKeywords(topKeywords)       // ★ 추가됨
                
                .visitLabels(visitLabels)
                .visitData(visitData)
                .rankLabels(rankLabels)
                .rankData(rankData)
                
                .currentRevenue(currentRevenue)
                .targetRevenue(targetRevenue)
                .reservationRate(reservationRate)
                .noShowRate(noShowRate)
                .noticeList(noticeList)
                .recentLogs(recentLogs)
                .build();
    }
}