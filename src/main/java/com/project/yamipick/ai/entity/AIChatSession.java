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
}
