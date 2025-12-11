package com.project.yamipick.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblUser")
public class User {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqUserGen")
    @SequenceGenerator(
            name = "seqUserGen",
            sequenceName = "seqUser",
            allocationSize = 1
    )
    private Long seqUser;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "id", nullable = false, length = 30, unique = true) 
    private String userId; 

    @Column(nullable = false, length = 300)
    private String password;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 100)
    private String role;

    @Column(nullable = false)
    private Integer penaltyScore;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "statusUser", length = 50)
    @ColumnDefault("'ACTIVE'") // 기본값 활동중
    private String statusUser;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "suspendedUntil")
    private LocalDateTime suspendedUntil; 

    // [편의 메소드] 상태 변경 로직
    public void changeStatus(String newStatus, LocalDateTime until) {
        this.statusUser = newStatus;
        this.suspendedUntil = until; // 정지 날짜 설정 (없으면 null)
    }
    
    // 비즈니스 로직 (벌점 부여 - 필요시 사용)
    public void addPenalty(int score) {
        this.penaltyScore += score;
    }

}