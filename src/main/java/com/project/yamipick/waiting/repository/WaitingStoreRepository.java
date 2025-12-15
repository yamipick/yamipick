package com.project.yamipick.waiting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.WaitingStore;

public interface WaitingStoreRepository extends JpaRepository<WaitingStore, Long> {
	
	// ★ [추가] 검색어(keyword)가 이름에 포함된 매장 찾기
    List<WaitingStore> findByNameContaining(String keyword);

    Optional<WaitingStore> findByOwnerId(Long ownerId);
}
