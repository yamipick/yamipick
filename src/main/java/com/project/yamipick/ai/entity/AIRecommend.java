package com.project.yamipick.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tblAIRecommend")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqRecommend")
    private Long seqRecommend;

    @Column(name = "seqMenu", nullable = false)
    private Long seqMenu;       // FK → tblMenu

    @Column(name = "userInput", length = 2000)
    private String userInput;

    @Column(name = "AIReason", length = 2000, nullable = false)
    private String aiReason;

    @Column(name = "AICreatedAt", nullable = false)
    private LocalDateTime aiCreatedAt;

    @Column(name = "seqSession")
    private Long seqSession;    // FK → tblAIChatSession
}
