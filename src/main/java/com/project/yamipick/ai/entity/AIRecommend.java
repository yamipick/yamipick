package com.project.yamipick.ai.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tblAIRecommend")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommend {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AI_RECOMMEND_SEQ")
	@SequenceGenerator(name = "AI_RECOMMEND_SEQ", sequenceName = "SEQRECOMMEND", allocationSize = 1)
    private Long seqRecommend;

    @Column(name = "seqMenu", nullable = false)
    private Long seqMenu;

    @Column(name = "userInput", length = 2000)
    private String userInput;

    @Column(name = "aiReason", length = 2000, nullable = false)
    private String aiReason;

    @Column(name = "aiCreatedAt", nullable = false)
    private LocalDateTime aiCreatedAt;

    @Column(name = "seqSession")
    private Long seqSession;

    // ====== 자동 시간 생성용 편의 메서드 ======
    @PrePersist
    public void prePersist() {
        if (this.aiCreatedAt == null) {
            this.aiCreatedAt = LocalDateTime.now();
        }
    }
	
}
