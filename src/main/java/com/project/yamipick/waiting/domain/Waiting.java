package com.project.yamipick.waiting.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqStore")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqUser")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seqWaitingStatus")
    private WaitingStatus waitingStatus;

    @Column(name = "timeSize")
    private int teamSize;

    // ★ [수정] 옵션 제거 -> Java에서 설정한 값이 DB에 저장됨
    @Column(name = "regdate") 
    private LocalDateTime regDate;
    
    @Column(name = "calledTime")
    private LocalDateTime calledTime;
    
    @Column(name = "seatedTime")
    private LocalDateTime seatedTime;
    
    @PrePersist
    public void prePersist() {
        // 이 값이 DB에 저장됩니다.
        if (this.regDate == null) this.regDate = LocalDateTime.now();
    }
}