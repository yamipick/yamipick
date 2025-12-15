package com.project.yamipick.waiting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.WaitingNotice;

public interface WaitingNoticeRepository extends JpaRepository<WaitingNotice, Long> {
    
    // 특정 매장의 공지사항 조회 (정렬: 고정글 우선 -> 최신순)
    // OrderByIsPinnedDesc: Y가 N보다 뒤에 나오므로 Desc(내림차순) 해야 Y가 먼저 나옴
    List<WaitingNotice> findByStoreIdOrderByIsPinnedDescRegDateDesc(Long storeId);
}
