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
@Table(name = "tblAIChatLog")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqAIChatLog;

    @Column(nullable = false)
    private Long seqUser;

    @Column(nullable = false, length = 2000)
    private String userInput;

    @Column(nullable = false, length = 5000)
    private String aiResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
