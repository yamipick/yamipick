package com.project.yamipick.waiting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.StoreSchedule;

public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {
    
    // 특정 매장의 특정 요일(0~6) 스케줄 찾기
    Optional<StoreSchedule> findByStoreIdAndDayOfWeek(Long storeId, int dayOfWeek);
}
