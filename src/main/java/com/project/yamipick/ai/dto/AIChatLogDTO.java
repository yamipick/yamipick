package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AIChatLogDTO {

	private Long seqAIChatLog;   // PK
    private Long seqUser;        // FK: 회원만 AI 사용 가능 (NOT NULL)
    private String userInput;    // 사용자가 입력한 질문
    private String aiResponse;   // AI가 답변한 내용
    private LocalDateTime createdAt; // 생성 시간
	
}
