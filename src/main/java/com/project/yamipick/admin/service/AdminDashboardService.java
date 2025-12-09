package com.project.yamipick.admin.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.admin.dto.DashboardDTO;
import com.project.yamipick.log.repository.BusinessLogQueryRepository;
import com.project.yamipick.notice.entity.Notice;
import com.project.yamipick.notice.repository.NoticeRepository;
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

    // 나중에 추가될 Repository들 (주석 처리)
    // private final PaymentRepository paymentRepository;
    // private final ReservationRepository reservationRepository;

    @Cacheable(value = "dashboardData")
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
                // DB에서 가져온 원본 메시지 (예: "바나프레소 선릉점 클릭됨")
                String rawMessage = t.get(0, String.class);
                Long count = t.get(1, Long.class);

                // " 클릭됨" 글자 제거해서 식당 이름만 추출
                String storeName = rawMessage;
                if (storeName != null && storeName.contains(" 클릭됨")) {
                    storeName = storeName.replace(" 클릭됨", "");
                }
                // 만약 메시지가 그냥 식당 이름만 있다면 그대로 사용

                rankLabels.add(storeName); // 이제 "식당 53"이 아니라 "바나프레소 선릉점"으로 나옵니다!
                rankData.add(count);
            }
        } catch (Exception e) {
            e.printStackTrace(); // 에러 확인용 (운영 시엔 log.error 권장)
        }
        
        // [추가] 최신 공지사항 5개 조회
        // (JPA 기본 메소드 네이밍 규칙 사용 예시)
        List<Notice> noticeList = noticeRepository.findTop5ByOrderByCreatedAtDesc();
        
        // =============================================
        // [TODO] 팀원들이 기능 완성하면 여기를 진짜 코드로 교체!
        // =============================================
        // 4. 예약 이행률 (로그 분석 결과)
        int reservationRate = logQueryRepository.getReservationRate();

        // 5. [Still Dummy] 매출 및 노쇼 데이터
        // (이 데이터는 로그보다는 Payment나 Reservation 테이블을 직접 조회하는 것이 정확합니다)
        long currentRevenue = 3450000; 
        long targetRevenue = 5000000;
        int noShowRate = 12; // 추후 ReservationRepository에서 status='NOSHOW' count로 구현 권장

        return DashboardDTO.builder()
                .todayJoin(todayJoin)
                .totalUser(totalUser)
                .visitLabels(visitLabels)
                .visitData(visitData)
                .rankLabels(rankLabels)
                .rankData(rankData)
                .currentRevenue(currentRevenue)
                .targetRevenue(targetRevenue)
                .reservationRate(reservationRate) // ★ 실제 값 적용
                .noShowRate(noShowRate)
                .noticeList(noticeList)
                .build();
    }
}