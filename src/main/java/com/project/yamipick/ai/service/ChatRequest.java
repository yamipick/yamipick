package com.project.yamipick.ai.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private Long seqSession;   // null이면 새 세션 시작
    private String message;    // 사용자 메시지
}
