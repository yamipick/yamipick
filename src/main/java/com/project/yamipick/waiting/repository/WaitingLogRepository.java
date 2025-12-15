package com.project.yamipick.waiting.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.WaitingLog;

public interface WaitingLogRepository extends JpaRepository<WaitingLog, Long> {
    // 최신순 50개 조회
    List<WaitingLog> findTop50ByStoreIdOrderByRegDateDesc(Long storeId);
    
 // ★ [추가] 특정 매장의 특정 기간(하루) 로그 조회
    List<WaitingLog> findAllByStoreIdAndRegDateBetweenOrderByRegDateDesc(
            Long storeId, LocalDateTime start, LocalDateTime end);
}
