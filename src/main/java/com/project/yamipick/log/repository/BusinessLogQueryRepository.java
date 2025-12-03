package com.project.yamipick.log.repository;

import com.project.yamipick.log.entity.BusinessLog;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
     * [신규] 인기 식당 TOP 5 (대시보드 랭킹 차트용)
     * 조건: 최근 7일, 조회수(VIEW), 대상(STORE)
     * 결과: [식당ID, 조회수]
     */
    public List<Tuple> getTopPopularStores() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        return queryFactory
                .select(
                    businessLog.targetId, // 식당 ID
                    businessLog.count()   // 조회수
                )
                .from(businessLog)
                .where(
                    businessLog.createdAt.after(sevenDaysAgo), // 최근 7일
                    businessLog.actionType.eq("VIEW"),         // 상세페이지 본 것
                    businessLog.targetType.eq("STORE")         // 대상이 '식당'인 것
                )
                .groupBy(businessLog.targetId)       // 식당 ID로 그룹핑
                .orderBy(businessLog.count().desc()) // 조회수 높은 순
                .limit(5)                            // 상위 5개만
                .fetch();
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
}