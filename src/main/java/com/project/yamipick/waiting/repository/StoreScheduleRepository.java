package com.project.yamipick.waiting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.yamipick.waiting.domain.StoreSchedule;

public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {
    
	
    // 특정 매장의 특정 요일(0~6) 스케줄 찾기
    Optional<StoreSchedule> findByStoreIdAndDayOfWeek(Long storeId, int dayOfWeek);
    
    
    @Query("SELECT sch FROM StoreSchedule sch WHERE sch.store.id IN :storeIds AND sch.dayOfWeek = :dayOfWeek")
    List<StoreSchedule> findAllByStoreIdInAndDayOfWeek(
            @Param("storeIds") List<Long> storeIds, 
            @Param("dayOfWeek") int dayOfWeek);
    
    List<StoreSchedule> findAllByStoreId(Long storeId);
}
