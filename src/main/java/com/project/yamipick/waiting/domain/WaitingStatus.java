package com.project.yamipick.waiting.domain;

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
import lombok.Setter;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblWaitingStatus")
public class WaitingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ws_gen")
    @SequenceGenerator(name = "seq_ws_gen", sequenceName = "seqWaitingStatus", allocationSize = 1)
    @Column(name = "seqWaitingStatus")
    private Long id;

    // DDL에 적힌 컬럼명 "WaitingStatus"와 매핑
    @Column(name = "WaitingStatus", nullable = false, unique = true)
    private String statusName; 
}
