package com.project.yamipick.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.ReviewReport;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

}