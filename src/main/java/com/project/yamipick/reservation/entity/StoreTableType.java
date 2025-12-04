package com.project.yamipick.reservation.entity;

import com.project.yamipick.store.entity.Store;

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
import lombok.ToString;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblStoreTableType")
public class StoreTableType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqStoreTableGen")
    @SequenceGenerator(
            name = "seqStoreTableGen",
            sequenceName = "seqStoreTable",   // DB 시퀀스 이름
            allocationSize = 1
    )
    private Long seqStoreTable;   // PK

    @Column(nullable = false, length = 30)
    private String name;          // 예: 창가 2인석, 룸 4인석

    @Column(nullable = false)
    private Integer capacity;     // 한 자리 최대 인원 (예: 2, 4, 6)

    @Column(nullable = false)
    private Integer quantity;     // 이 타입의 자리 개수 (예: 창가 2인석 3개)

    // FK: 어떤 매장의 자리 타입인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQSTORE", nullable = false)
    private Store store;
}
