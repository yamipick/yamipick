package com.project.yamipick.waiting.domain;

import java.time.LocalDateTime;

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
@Table(name = "tblWaitingNotice") // ★ 수정하신 테이블명
public class WaitingNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_wn_gen")
    @SequenceGenerator(name = "seq_wn_gen", sequenceName = "seqWaitingNotice", allocationSize = 1)
    @Column(name = "seqWaitingNotice")
    private Long id;

    // 매장과 직접 연결 (영구 공지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqStore")
    private WaitingStore store; 

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;
    
    @Column(name = "isPinned")
    private String isPinned; // "Y" or "N"

    @Column(name = "regDate")
    private LocalDateTime regDate;
    
    @PrePersist
    public void prePersist() {
        if(this.regDate == null) this.regDate = LocalDateTime.now();
        if(this.isPinned == null) this.isPinned = "N";
    }
}