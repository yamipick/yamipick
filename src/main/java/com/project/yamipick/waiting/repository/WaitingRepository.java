package com.project.yamipick.waiting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.yamipick.waiting.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    // 1. 매장용 목록
    @Query("SELECT w FROM Waiting w WHERE w.store.id = :storeId AND w.waitingStatus.statusName IN :statuses ORDER BY w.regDate ASC")
    List<Waiting> findStoreList(@Param("storeId") Long storeId, @Param("statuses") List<String> statuses);

    // 2. 손님용 기록
    @Query("SELECT w FROM Waiting w WHERE w.member.id = :memberId AND w.waitingStatus.statusName IN :statuses ORDER BY w.regDate DESC")
    List<Waiting> findMemberHistory(@Param("memberId") Long memberId, @Param("statuses") List<String> statuses);

    // 3. 내 앞 대기 수
    @Query("SELECT COUNT(w) FROM Waiting w WHERE w.waitingStatus.statusName = 'WAITING' AND w.id < :myId")
    long countAhead(@Param("myId") Long myId);
}