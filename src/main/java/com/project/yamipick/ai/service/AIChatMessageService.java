package com.project.yamipick.ai.service;

import com.project.yamipick.ai.dto.AIChatMessageDTO;
import com.project.yamipick.ai.entity.AIChatMessage;
import com.project.yamipick.ai.repository.AIChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIChatMessageService {

    private final AIChatMessageRepository messageRepository;

    // 메시지 저장
    public AIChatMessageDTO saveMessage(AIChatMessageDTO dto) {

        AIChatMessage entity = AIChatMessage.builder()
                .seqMessage(dto.getSeqMessage())
                .seqSession(dto.getSeqSession())
                .senderType(dto.getSenderType())
                .messageText(dto.getMessageText())
                .messageCreatedAt(
                        dto.getMessageCreatedAt() != null ? dto.getMessageCreatedAt() : LocalDateTime.now()
                )
                .build();

        AIChatMessage saved = messageRepository.save(entity);

        return AIChatMessageDTO.builder()
                .seqMessage(saved.getSeqMessage())
                .seqSession(saved.getSeqSession())
                .senderType(saved.getSenderType())
                .messageText(saved.getMessageText())
                .messageCreatedAt(saved.getMessageCreatedAt())
                .build();
    }

    // 해당 세션의 메시지 목록 조회
    public List<AIChatMessageDTO> getMessagesBySession(Long seqSession) {
        return messageRepository.findBySeqSessionOrderByMessageCreatedAtAsc(seqSession)
                .stream()
                .map(m -> AIChatMessageDTO.builder()
                        .seqMessage(m.getSeqMessage())
                        .seqSession(m.getSeqSession())
                        .senderType(m.getSenderType())
                        .messageText(m.getMessageText())
                        .messageCreatedAt(m.getMessageCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
