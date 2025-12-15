package com.project.yamipick.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.yamipick.ai.entity.AIChatSession;

public interface AIChatSessionRepository extends JpaRepository<AIChatSession, Long> {

}
