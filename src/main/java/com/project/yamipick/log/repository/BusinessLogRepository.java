package com.project.yamipick.log.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.log.entity.BusinessLog;

// JPA 기본 기능 (저장, 삭제 등) 담당
public interface BusinessLogRepository extends JpaRepository<BusinessLog, Long> {
}