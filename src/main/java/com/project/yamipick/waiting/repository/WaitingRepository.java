package com.project.yamipick.waiting.repository;

import java.util.List;

import org.apache.catalina.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.Member;
import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.domain.WaitingStatus;



public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    // 매장별 대기 목록 (대기중, 호출됨 상태만)
    List<Waiting> findByStoreIdAndStatusInOrderByRegDateAsc(Long storeId, List<WaitingStatus> statuses);
    // 내 앞 대기 수
    long countByStatusAndIdLessThan(WaitingStatus status, Long id);
}