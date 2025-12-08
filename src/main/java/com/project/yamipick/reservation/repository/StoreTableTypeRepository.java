package com.project.yamipick.reservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.reservation.entity.StoreTableType;
import com.project.yamipick.store.entity.Store;

public interface StoreTableTypeRepository extends JpaRepository<StoreTableType, Long> {

    // 특정 매장의 테이블 타입 목록
    List<StoreTableType> findByStore_SeqStore(Long seqStore);
    
    List<StoreTableType> findByStore(Store store);
}
