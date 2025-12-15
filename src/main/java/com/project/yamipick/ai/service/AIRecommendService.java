package com.project.yamipick.ai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.AIRecommendDTO;
import com.project.yamipick.ai.entity.AIRecommend;
import com.project.yamipick.ai.repository.AIRecommendRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIRecommendService {

    private final AIRecommendRepository recommendRepository;

    // 🔥 추천 결과 저장
    public AIRecommendDTO saveRecommend(AIRecommendDTO dto) {

        // seqSession 이 null이면 저장 X (일반 태그 추천)
        // seqSession 이 있으면 저장 O (AI 챗봇 추천)

        Long sessionValue = dto.getSeqSession() != null ? dto.getSeqSession() : null;

        AIRecommend entity = AIRecommend.builder()
                .seqRecommend(dto.getSeqRecommend())
                .seqMenu(dto.getSeqMenu())
                .userInput(dto.getUserInput())
                .aiReason(dto.getAiReason())
                .aiCreatedAt(
                        dto.getAiCreatedAt() != null ? dto.getAiCreatedAt() : LocalDateTime.now()
                )
                .seqSession(sessionValue)   // ⭐ 조건부 저장 핵심 부분
                .build();

        AIRecommend saved = recommendRepository.save(entity);

        return AIRecommendDTO.builder()
                .seqRecommend(saved.getSeqRecommend())
                .seqMenu(saved.getSeqMenu())
                .userInput(saved.getUserInput())
                .aiReason(saved.getAiReason())
                .aiCreatedAt(saved.getAiCreatedAt())
                .seqSession(saved.getSeqSession()) // null or 값
                .build();
    }

    // 🔥 세션 기준 추천 조회
    public List<AIRecommendDTO> getRecommendsBySession(Long seqSession) {

        List<AIRecommend> list = recommendRepository.findBySeqSession(seqSession);

        return list.stream()
                .map(r -> AIRecommendDTO.builder()
                        .seqRecommend(r.getSeqRecommend())
                        .seqMenu(r.getSeqMenu())
                        .userInput(r.getUserInput())
                        .aiReason(r.getAiReason())
                        .aiCreatedAt(r.getAiCreatedAt())
                        .seqSession(r.getSeqSession())
                        .build())
                .collect(Collectors.toList());
    }
}

