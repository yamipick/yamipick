package com.project.yamipick.log.repository;

import com.project.yamipick.log.entity.BusinessLog;
import com.querydsl.core.Tuple;
import com.project.yamipick.log.entity.QBusinessLog;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;

import java.time.LocalDateTime;
import java.util.List;

// Q파일 임포트 (빌드 후 자동 생성됨)
import static com.project.yamipick.log.entity.QBusinessLog.businessLog;

@Repository // 스프링 빈으로 등록
@RequiredArgsConstructor
public class BusinessLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * [기존] 동적 검색 메소드 (로그 관리 페이지용)
     */
    public List<BusinessLog> searchLogs(String actionType, String userId) {
        return queryFactory
                .selectFrom(businessLog)
                .where(
                    eqActionType(actionType), // 조건 1
                    eqUserId(userId)          // 조건 2
                )
                .orderBy(businessLog.seqLog.desc()) // 최신순
                .limit(1000) // 1000개만 조회
                .fetch();
    }

    /**
     * [신규] 최근 7일간 일별 방문자 수 통계 (대시보드 선 그래프용)
     * 결과: [년, 월, 일, 방문수]
     */
    public List<Tuple> getDailyVisitStats() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        return queryFactory
                .select(
                        businessLog.createdAt.year(),
                        businessLog.createdAt.month(),
                        businessLog.createdAt.dayOfMonth(),
                        businessLog.count() // 방문 횟수
                )
                .from(businessLog)
                .where(
                        businessLog.createdAt.after(sevenDaysAgo)
                )
                .groupBy(
                        businessLog.createdAt.year(),
                        businessLog.createdAt.month(),
                        businessLog.createdAt.dayOfMonth()
                )
                .orderBy(
                        businessLog.createdAt.year().asc(),
                        businessLog.createdAt.month().asc(),
                        businessLog.createdAt.dayOfMonth().asc()
                )
                .fetch();
    }

    /**
     * [수정] 핫플레이스 TOP 5 (CLOB 에러 해결 버전)
     * Oracle ORA-00932 해결: CLOB 컬럼은 바로 Group By를 할 수 없으므로,
     * DBMS_LOB.SUBSTR 함수를 사용해 문자열로 변환 후 처리합니다.
     */
    public List<Tuple> getTopPopularStores() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        // ★ 핵심: CLOB(message)을 VARCHAR(String)로 변환하는 템플릿 생성
        // (내용의 앞쪽 1000자만 잘라서 비교하겠다는 의미, 그룹핑 가능해짐)
        StringTemplate messageAsString = Expressions.stringTemplate(
            "DBMS_LOB.SUBSTR({0}, 1000, 1)", 
            businessLog.message
        );

        return queryFactory
                .select(
                    messageAsString,    // 변환된 메시지 선택
                    businessLog.count()
                )
                .from(businessLog)
                .where(
                    businessLog.createdAt.after(sevenDaysAgo),
                    businessLog.actionType.eq("CLICK"),
                    businessLog.targetType.eq("MAP_MARKER")
                )
                .groupBy(messageAsString) // ★ 변환된 메시지로 그룹핑
                .orderBy(businessLog.count().desc())
                .limit(5)
                .fetch();
    }
    
    /**
     * [신규] 예약 이행률 계산 (로그 기반)
     * 로직: (예약 성공 횟수 / 예약 버튼 클릭 횟수) * 100
     */
    public int getReservationRate() {
        // 1. 예약 시도: '예약하기' 버튼을 클릭한 로그 수
        long attemptCount = queryFactory
                .select(businessLog.count())
                .from(businessLog)
                .where(businessLog.message.contains("예약하기 버튼")) // 로그 메시지 기준
                .fetchOne();

        // 2. 예약 성공: ReservationController.create 메소드가 SUCCESS로 끝난 로그 수
        long successCount = queryFactory
                .select(businessLog.count())
                .from(businessLog)
                .where(
                    businessLog.actionType.eq("AUTO_LOG"),         // 자동 로그
                    businessLog.targetType.eq("ReservationController"), // 대상 컨트롤러
                    businessLog.targetId.eq("SUCCESS"),            // 성공 여부
                    businessLog.message.startsWith("create")       // create 메소드
                )
                .fetchOne();

        // 3. 비율 계산 (0으로 나누기 방지)
        if (attemptCount == 0) return 0;
        
        return (int) ((double) successCount / attemptCount * 100);
    }

    // --- BooleanExpression 조건들 (null이면 무시됨) ---

    private BooleanExpression eqActionType(String actionType) {
        if (actionType == null || actionType.isEmpty()) {
            return null; // 조건 무시 (전체 조회)
        }
        return businessLog.actionType.eq(actionType);
    }

    private BooleanExpression eqUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        return businessLog.userId.contains(userId); // LIKE 검색
    }
    
    // 인기 검색어 TOP 5 조회
    public List<String> getTopSearchKeywords() {
        QBusinessLog log = QBusinessLog.businessLog;
        
        return queryFactory
                .select(log.targetId) // 검색어(keyword)가 targetId에 저장됨
                .from(log)
                .where(log.actionType.eq("SEARCH")) // 검색 로그만
                .groupBy(log.targetId)
                .orderBy(log.targetId.count().desc()) // 많이 검색된 순
                .limit(5)
                .fetch();
    }
}