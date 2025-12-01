package com.project.yamipick.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.ai.entity.AIChatLog;

public interface AIChatLogRepository extends JpaRepository<AIChatLog, Long> {

}
