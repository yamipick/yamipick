package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import com.project.yamipick.ai.entity.AIChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatMessageDTO {

    private Long seqMessage;
    private Long seqSession;
    private String senderType;    // USER / AI
    private String messageText;
    private LocalDateTime messageCreatedAt;

    public AIChatMessage toEntity() {
        return AIChatMessage.builder()
                .seqMessage(this.seqMessage)
                .seqSession(this.seqSession)
                .senderType(this.senderType)
                .messageText(this.messageText)
                .messageCreatedAt(this.messageCreatedAt)
                .build();
    }
}
