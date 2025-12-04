package com.project.yamipick.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tblStoreSchedule")
public class StoreSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sch_gen")
    @SequenceGenerator(name = "seq_sch_gen", sequenceName = "seqStoreSchedule", allocationSize = 1)
    @Column(name = "seqSchedule")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqStore")
    private WaitingStore store;

    // 요일 (0:일, 1:월, 2:화, 3:수, 4:목, 5:금, 6:토)
    @Column(name = "dayOfWeek")
    private int dayOfWeek;

    @Column(name = "openTime") // 예: "10:00"
    private String openTime;

    @Column(name = "closeTime") // 예: "22:00"
    private String closeTime;

    @Column(name = "breakStart")
    private String breakStart;

    @Column(name = "breakEnd")
    private String breakEnd;

    // ★ [요청반영] 휴무여부가 아니라 영업여부 (Y:영업함, N:휴무)
    @Column(name = "isOpen") 
    private String isOpen;
}