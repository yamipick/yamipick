package com.project.yamipick.report.repository;

import com.project.yamipick.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 최신순 정렬
    List<Report> findAllByOrderByCreatedAtDesc();

    // 처리 상태별 조회 (예: 'PENDING'만 보기)
    List<Report> findByStatusOrderByCreatedAtDesc(String status);
}