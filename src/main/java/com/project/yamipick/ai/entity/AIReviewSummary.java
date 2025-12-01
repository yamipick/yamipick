package com.project.yamipick.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tblAIReviewSummary")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqSummary")
    private Long seqSummary;

    @Column(name = "restaurantId", nullable = false, length = 50)
    private String restaurantId;

    @Lob
    @Column(name = "summaryPublic", nullable = false)
    private String summaryPublic;

    @Lob
    @Column(name = "summaryMember", nullable = false)
    private String summaryMember;

    @Column(name = "summaryCreatedAt", nullable = false)
    private LocalDateTime summaryCreatedAt;

    @Column(name = "summaryUpdatedAt")
    private LocalDateTime summaryUpdatedAt;
}
