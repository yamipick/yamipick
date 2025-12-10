package com.project.yamipick.reservation.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 특정 유저의 전체 예약 내역
    List<Reservation> findByUser_SeqUserOrderByReserveDateDescReserveTimeDesc(Long seqUser);

    // 특정 매장의 특정 날짜 예약 목록
    List<Reservation> findByStore_SeqStoreAndReserveDate(Long seqStore, LocalDate date);

    // 특정 매장 + 테이블 타입 + 날짜별 예약 목록
    List<Reservation> findByStore_SeqStoreAndStoreTableType_SeqStoreTableAndReserveDate(
            Long seqStore,
            Long seqStoreTable,
            LocalDate date
    );
    
    List<Reservation> findByStore_SeqStoreAndStoreTableType_SeqStoreTableAndReserveDateAndReserveTime(
            Long seqStore,
            Long seqStoreTable,
            LocalDate reserveDate,
            String reserveTime
    );
    
}
