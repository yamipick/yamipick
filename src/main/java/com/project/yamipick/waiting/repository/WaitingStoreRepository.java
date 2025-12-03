package com.project.yamipick.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.WaitingStore;

public interface WaitingStoreRepository extends JpaRepository<WaitingStore, Long> {

}
