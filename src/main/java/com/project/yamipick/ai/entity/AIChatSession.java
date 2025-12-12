package com.project.yamipick.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tblAIChatSession")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatSession {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AI_CHAT_SESSION_SEQ")
	@SequenceGenerator(
	        name = "AI_CHAT_SESSION_SEQ",
	        sequenceName = "SEQSESSION",  // DB 시퀀스 이름
	        allocationSize = 1
	)
    private Long seqSession;

    @Column(name = "sessionCreatedAt", nullable = false)
    private LocalDateTime sessionCreatedAt;

    @Column(name = "seqUser", nullable = false)
    private Long seqUser;   // FK → tblUser.seqUser
    
    // 이전 대화에서 추출된 positive 태그
    @Column(length = 500)
    private String lastPositiveTags;

    // 이전 대화에서 추출된 negative
    @Column(length = 500)
    private String lastNegativeTags;

    // 이전 감정
    @Column(length = 50)
    private String lastEmotion;
    
    //이전 추천 메뉴
    @Column(name = "lastRecommendMenuId")
    private Long lastRecommendMenuId;
}
