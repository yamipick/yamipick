package com.project.yamipick.waiting.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.WaitingOperation;

public interface WaitingOperationRepository extends JpaRepository<WaitingOperation, Long> {
    // 특정 매장의 특정 날짜 운영 정보 찾기
    Optional<WaitingOperation> findByStoreIdAndOperationDate(Long storeId, LocalDate operationDate);
}
