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
    private Long seqSession;	//FK
    private SenderType senderType;
    private String messageText;
    private LocalDateTime messageCreatedAt;

    public enum SenderType {
        USER, AI
    }
    
    public AIChatMessage toEntity() {
        return AIChatMessage.builder()
                .seqMessage(this.seqMessage)
                .seqSession(this.seqSession)
                .senderType(
                    this.senderType != null 
                    ? this.senderType.name()   // enum → String 변환
                    : null
                )
                .messageText(this.messageText)
                .messageCreatedAt(
                    this.messageCreatedAt != null 
                    ? this.messageCreatedAt 
                    : LocalDateTime.now()
                )
                .build();
    }
}
