package com.project.yamipick.ai.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.AIChatSessionDTO;
import com.project.yamipick.ai.dto.ExtractedTags;
import com.project.yamipick.ai.entity.AIChatSession;
import com.project.yamipick.ai.repository.AIChatSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIChatSessionService {

    private final AIChatSessionRepository repo;
    

    // 세션 생성 → seqSession(Long)만 반환
    public Long createSession(Long seqUser) {

        AIChatSession session = AIChatSession.builder()
                .seqUser(seqUser)
                .sessionCreatedAt(LocalDateTime.now())
                .lastPositiveTags(null)
                .lastNegativeTags(null)
                .lastEmotion(null)
                .build();

        AIChatSession saved = repo.save(session);
        return saved.getSeqSession();
    }
    
    // 세션 존재여부 확인
    public boolean exists(Long seqSession) {
        return repo.existsById(seqSession);
    }
    
    //세션 조회 (엔티티 그대로 반환)
    public AIChatSession getEntity(Long seqSession) {
    	return repo.findById(seqSession)
    			.orElse(null);
    }

    // 세션 조회 (DTO 형태 반환)
    public AIChatSessionDTO getSession(Long seqSession) {

        AIChatSession session = repo.findById(seqSession)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));

        return AIChatSessionDTO.builder()
                .seqSession(session.getSeqSession())
                .sessionCreatedAt(session.getSessionCreatedAt())
                .seqUser(session.getSeqUser())
                .build();
    }
    
    //태그 & 감정 저장
    public void updateTags(Long sessionId,
    					   List<String> pos,
    					   List<String> neg,
    					   String emotion) {
    	AIChatSession s = repo.findById(sessionId).orElseThrow(() -> new RuntimeException("세션 없음"));
    	    	
    	s.setLastPositiveTags(pos == null ? null : String.join(",", pos));
    	s.setLastNegativeTags(neg == null ? null : String.join(",", neg));
    	s.setLastEmotion(emotion);    	
    }
    
    //저장된 태그 불러오기
    public ExtractedTags loadLastTags(Long sessionId) {
    	AIChatSession s = repo.findById(sessionId).orElse(null);
    	
    	if (s == null) {
    		return new ExtractedTags(List.of(), List.of());
    	}
    	
    	return new ExtractedTags(
    			parseCSV(s.getLastPositiveTags()),
    			parseCSV(s.getLastNegativeTags())
    	);
    }
    
    //CSV -> List 파서
    private List<String> parseCSV(String str) {
    	if (str == null || str.isBlank()) return List.of();
    	
    	return Arrays.stream(str.split(","))
    			.map(String::trim)
    			.filter(s -> !s.isBlank())
    			.toList();
    }
    
}
