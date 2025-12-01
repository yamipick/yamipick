package com.project.yamipick.ai.dto;

import com.project.yamipick.ai.entity.AIChatSession;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatSessionDTO {

    private Long seqSession;
    private LocalDateTime sessionCreatedAt;
    private Long seqUser;

    public AIChatSession toEntity() {
        return AIChatSession.builder()
                .seqSession(this.seqSession)
                .sessionCreatedAt(this.sessionCreatedAt)
                .seqUser(this.seqUser)
                .build();
    }
}
