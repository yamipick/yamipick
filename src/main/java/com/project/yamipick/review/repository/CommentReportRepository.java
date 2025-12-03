package com.project.yamipick.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.CommentReport;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

}