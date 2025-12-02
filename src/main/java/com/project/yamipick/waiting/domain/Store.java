package com.project.yamipick.waiting.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = "tblStore")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_store_gen")
    @SequenceGenerator(name = "seq_store_gen", sequenceName = "seqStore", allocationSize = 1)
    @Column(name = "seqStore")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "kakaoPlaceId", unique = true, nullable = false)
    private String kakaoPlaceId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "seqUser")
    private Long ownerId;

    // ★ DB에 없음! 메모리에서만 사용 (영업 상태 토글용)
    @Transient 
    @Builder.Default
    private boolean waitingOpen = true; 
}