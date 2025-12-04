package com.project.yamipick.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    // null이면 새 세션 생성, 아니면 기존 세션 이어서 사용
    private Long seqSession;

    // 사용자 입력 메시지
    private String message;
}
