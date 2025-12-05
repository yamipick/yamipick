package com.project.yamipick.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.yamipick.waiting.domain.WaitingOperation;

import jakarta.persistence.LockModeType;

public interface WaitingOperationRepository extends JpaRepository<WaitingOperation, Long> {
    // 특정 매장의 특정 날짜 운영 정보 찾기
	@Lock(LockModeType.PESSIMISTIC_WRITE) 
    @Query("SELECT op FROM WaitingOperation op WHERE op.store.id = :storeId AND op.operationDate = :date")
    Optional<WaitingOperation> findByStoreIdAndOperationDate(
            @Param("storeId") Long storeId, 
            @Param("date") LocalDate date);
	
	
	// ★ [추가] 매장 ID 리스트로 오늘 운영 정보 모두 조회
    @Query("SELECT op FROM WaitingOperation op WHERE op.store.id IN :storeIds AND op.operationDate = :date")
    List<WaitingOperation> findAllByStoreIdInAndOperationDate(
            @Param("storeIds") List<Long> storeIds, 
            @Param("date") LocalDate date);
}
