package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.project.yamipick.ai.entity.AIChatSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
                .sessionCreatedAt(
                        this.sessionCreatedAt != null 
                        ? this.sessionCreatedAt 
                        : LocalDateTime.now()
                )
                .seqUser(this.seqUser)
                .build();
    }
}
