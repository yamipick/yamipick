package com.project.yamipick.waiting.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.yamipick.waiting.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    // 1. 매장용 목록 (오늘 날짜 + 특정 매장).
    @Query("SELECT w FROM Waiting w " +
           "JOIN FETCH w.member m " + 
           "WHERE w.operation.store.id = :storeId " +
           "AND w.operation.operationDate = :today " +
           "AND w.waitingStatus.statusName IN :statuses " +
           "ORDER BY w.waitingNumber ASC")
    List<Waiting> findStoreList(@Param("storeId") Long storeId, 
                                @Param("today") LocalDate today, 
                                @Param("statuses") List<String> statuses);

    // 2. 손님용 기록
    @Query("SELECT w FROM Waiting w WHERE w.member.id = :memberId AND w.waitingStatus.statusName IN :statuses ORDER BY w.regDate DESC")
    List<Waiting> findMemberHistory(@Param("memberId") Long memberId, @Param("statuses") List<String> statuses);

    // 3. 내 앞 대기 수
    @Query("SELECT COUNT(w) FROM Waiting w " +
           "WHERE w.operation.id = :opId " +
           "AND w.waitingStatus.statusName = 'WAITING' " +
           "AND w.waitingNumber < :myNum")
    long countAhead(@Param("opId") Long opId, @Param("myNum") int myNum);
    
    // 4. 유효한 웨이팅 찾기
    @Query("SELECT w FROM Waiting w " +
           "WHERE w.member.id = :memberId " +
           "AND w.waitingStatus.statusName IN :statuses " +
           "ORDER BY w.regDate DESC")
    List<Waiting> findActiveWaiting(@Param("memberId") Long memberId, @Param("statuses") List<String> statuses);
}