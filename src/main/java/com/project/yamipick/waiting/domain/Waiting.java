package com.project.yamipick.waiting.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblWaiting")
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_w_gen")
    @SequenceGenerator(name = "seq_w_gen", sequenceName = "seqWaiting", allocationSize = 1)
    @Column(name = "seqWaiting")
    private Long id;

    // ★ [변경] Store 대신 Operation 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqOperation")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private WaitingOperation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqUser")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private WaitingMember member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seqWaitingStatus")
    private WaitingStatus waitingStatus;

    // ★ [추가] 고정된 대기번호 (1번, 2번...)
    @Column(name = "waitingNumber")
    private int waitingNumber;

    @Column(name = "teamSize")
    private int teamSize;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "regDate") 
    private LocalDateTime regDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "calledTime")
    private LocalDateTime calledTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "seatedTime")
    private LocalDateTime seatedTime;
    
    @PrePersist
    public void prePersist() {
        if (this.regDate == null) this.regDate = LocalDateTime.now();
    }
}