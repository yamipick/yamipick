package com.project.yamipick.ai.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tblReviewSummary")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqReviewSummary;

    private Long seqUser; // 회원 요약이면 FK, 비회원이면 null

    @Column(nullable = false)
    private Long placeId;

    @Column(nullable = false, length = 5000)
    private String summaryText;

    private Integer reviewCount;
    private String sentiment;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
