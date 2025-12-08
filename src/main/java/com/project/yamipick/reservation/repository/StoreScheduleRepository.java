package com.project.yamipick.reservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.reservation.entity.StoreSchedule;
import com.project.yamipick.store.entity.Store;

public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {

    // 특정 매장의 전체 스케줄 (0~6 요일)
    List<StoreSchedule> findByStore_SeqStoreOrderByDayOfWeek(Long seqStore);
    
    List<StoreSchedule> findByStore(Store store);
}
