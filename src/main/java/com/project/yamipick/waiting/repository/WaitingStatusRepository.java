package com.project.yamipick.waiting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.WaitingStatus;

public interface WaitingStatusRepository extends JpaRepository<WaitingStatus, Long> {
    Optional<WaitingStatus> findByStatusName(String statusName);
}
