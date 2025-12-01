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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqSession")
    private Long seqSession;

    @Column(name = "sessionCreatedAt", nullable = false)
    private LocalDateTime sessionCreatedAt;

    @Column(name = "seqUser", nullable = false)
    private Long seqUser;   // FK → tblUser.seqUser
}
