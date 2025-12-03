package com.project.yamipick.ai.service;

import com.project.yamipick.ai.dto.AIChatSessionDTO;
import com.project.yamipick.ai.entity.AIChatSession;
import com.project.yamipick.ai.repository.AIChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AIChatSessionService {

    private final AIChatSessionRepository sessionRepository;

    // 세션 생성
    public AIChatSessionDTO createSession(Long seqUser) {
        AIChatSession session = AIChatSession.builder()
                .seqUser(seqUser)
                .sessionCreatedAt(LocalDateTime.now())
                .build();

        AIChatSession saved = sessionRepository.save(session);

        return AIChatSessionDTO.builder()
                .seqSession(saved.getSeqSession())
                .sessionCreatedAt(saved.getSessionCreatedAt())
                .seqUser(saved.getSeqUser())
                .build();
    }

    // 세션 단건 조회
    public AIChatSessionDTO getSession(Long seqSession) {

        AIChatSession session = sessionRepository.findById(seqSession)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));

        return AIChatSessionDTO.builder()
                .seqSession(session.getSeqSession())
                .sessionCreatedAt(session.getSessionCreatedAt())
                .seqUser(session.getSeqUser())
                .build();
    }
}
