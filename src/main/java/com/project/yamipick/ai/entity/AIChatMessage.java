package com.project.yamipick.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tblAIChatMessage")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqMessage")
    private Long seqMessage;

    @Column(name = "seqSession", nullable = false)
    private Long seqSession;   // FK → tblAIChatSession.seqSession

    @Column(name = "senderType", nullable = false, length = 10)
    private String senderType;   // USER / AI

    @Lob
    @Column(name = "messageText", nullable = false)
    private String messageText;

    @Column(name = "messageCreatedAt", nullable = false)
    private LocalDateTime messageCreatedAt;
}
