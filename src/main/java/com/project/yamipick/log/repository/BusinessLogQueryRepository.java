package com.project.yamipick.log.repository;

import com.project.yamipick.log.entity.BusinessLog;
import com.project.yamipick.log.entity.QBusinessLog;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// Q파일 임포트 (빌드 후 자동 생성됨)
import static com.project.yamipick.log.entity.QBusinessLog.businessLog;

@Repository // 스프링 빈으로 등록
@RequiredArgsConstructor
public class BusinessLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    // ★ 동적 검색 메소드
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

    // --- 아래는 조건들 (null이면 무시됨) ---

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