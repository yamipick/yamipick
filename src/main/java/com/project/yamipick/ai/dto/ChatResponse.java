package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private Long seqSession;
    private String userMessage;
    private String aiMessage;
    private LocalDateTime timestamp;
    
    private List<MenuRecommendResponse> recommendList;

}

