package com.project.yamipick.report.repository;

import com.project.yamipick.report.entity.QReport;
import com.project.yamipick.report.entity.Report;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepository {

    private final JPAQueryFactory queryFactory;
    QReport report = QReport.report;

    // 신고 목록 조회 (상태별 필터링 + 페이징)
    public Page<Report> searchReports(String status, Pageable pageable) {
        
        List<Report> content = queryFactory
                .selectFrom(report)
                .where(eqStatus(status)) // 상태 조건 (PENDING 등)
                .orderBy(report.seqReport.desc()) // 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(report.count())
                .from(report)
                .where(eqStatus(status))
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }

    private BooleanExpression eqStatus(String status) {
        if (status == null || status.isEmpty() || "ALL".equals(status)) {
            return null; // 전체 조회
        }
        return report.status.eq(status);
    }
}