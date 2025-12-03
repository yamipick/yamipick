package com.project.yamipick.waiting.domain;

import java.time.LocalDate;

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
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "tblWaitingOperation", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"seqStore", "operationDate"}))
public class WaitingOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_oper_gen")
    @SequenceGenerator(name = "seq_oper_gen", sequenceName = "seqWaitingOperation", allocationSize = 1)
    @Column(name = "seqOperation")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqStore")
    private Store store;

    @Column(name = "operationDate", nullable = false)
    private LocalDate operationDate; // 2025-12-02

    @Column(name = "status") 
    @Builder.Default
    private String status = "CLOSED"; // OPEN, CLOSED, BREAK

    @Column(name = "lastWaitingNum")
    @Builder.Default
    private int lastWaitingNum = 0; // 0부터 시작
}
