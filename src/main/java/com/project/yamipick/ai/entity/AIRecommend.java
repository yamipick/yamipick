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
@Table(name = "tblAIRecommend")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqAIRecommend;

    private Long seqUser; // 비회원일 경우 null

    @Column(nullable = false)
    private String category;

    private String menuName;
    private String storeName;
    private String address;
    private String priceRange;

    @Column(length = 2000)
    private String imageUrl;

    @Column(length = 2000)
    private String reason; // GPT 추천 이유

    private String source; // GPT / TOURAPI 등

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
