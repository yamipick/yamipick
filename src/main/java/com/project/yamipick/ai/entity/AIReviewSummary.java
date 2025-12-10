package com.project.yamipick.ai.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tblAIReviewSummary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIReviewSummary {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long seqSummary;
	
	@Column(nullable = false, length = 50)
	private String restaurantId;
	
	@Lob
	@Column(nullable = false)
	private String summaryPublic;
	
	@Lob
	@Column(nullable = false)
	private String summaryMember;
	
	@Column(nullable = false)
	private LocalDateTime summaryCreatedAt;
	
	private LocalDateTime summaryUpdatedAt;
	
	//생성 메서드
	public static AIReviewSummary create(String restaurantId, String summaryPublic, String summaryMember) {
		AIReviewSummary s = new AIReviewSummary();
        s.restaurantId = restaurantId;
        s.summaryPublic = summaryPublic;
        s.summaryMember = summaryMember;
        s.summaryCreatedAt = LocalDateTime.now();
        return s;
	}
	
	// 수정 메서드
    public void update(String summaryPublic, String summaryMember) {
        this.summaryPublic = summaryPublic;
        this.summaryMember = summaryMember;
        this.summaryUpdatedAt = LocalDateTime.now();
    }
}
