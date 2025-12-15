package com.project.yamipick.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.yamipick.ai.entity.AIChatMessage;

import java.util.List;

public interface AIChatMessageRepository extends JpaRepository<AIChatMessage, Long> {

    // 하나의 세션에 속한 메시지들 조회
    List<AIChatMessage> findBySeqSessionOrderByMessageCreatedAtAsc(Long seqSession);
}
