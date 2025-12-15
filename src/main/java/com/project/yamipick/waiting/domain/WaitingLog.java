package com.project.yamipick.waiting.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblWaitingLog") // ★ 수정된 테이블 이름
public class WaitingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_wl_gen")
    @SequenceGenerator(name = "seq_wl_gen", sequenceName = "seqWaitingLog", allocationSize = 1)
    @Column(name = "seqWaitingLog")
    private Long id;

    @Column(name = "seqStore")
    private Long storeId; // FK (단순 ID 저장)

    @Column(name = "seqWaiting")
    private Long waitingId; // FK (Nullable)

    @Column(name = "actionType")
    private String actionType; // CALL, ENTER, CANCEL...

    @Column(name = "logMessage")
    private String logMessage; // 로그 내용
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "regDate")
    private LocalDateTime regDate;
    
    @PrePersist
    public void prePersist() {
        if(this.regDate == null) this.regDate = LocalDateTime.now();
    }
}
