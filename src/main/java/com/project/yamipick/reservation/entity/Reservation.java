package com.project.yamipick.reservation.entity;

import java.time.LocalDate;

import com.project.yamipick.store.entity.Store;
import com.project.yamipick.user.entity.User;

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
@Table(name = "tblReservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqReservationGen")
    @SequenceGenerator(
            name = "seqReservationGen",
            sequenceName = "seqReservation",   // DB 시퀀스 이름
            allocationSize = 1
    )
    private Long seqReservation;   // PK

    @Column(nullable = false)
    private LocalDate reserveDate; // 예약 날짜 (DATE 컬럼 매핑)

    @Column(nullable = false, length = 5)
    private String reserveTime;    // 'HH:MM'

    @Column(nullable = false)
    private Integer peopleCount;   // 인원 수

    @Column(nullable = false, length = 10)
    private String status;         // '대기', '완료', '취소', '노쇼' 등

    // 예약한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQUSER", nullable = false)
    private User user;

    // 예약한 매장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQSTORE", nullable = false)
    private Store store;

    // 예약한 자리 타입 (창가/룸 등)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQSTORETABLE", nullable = false)
    private StoreTableType storeTableType;
    
    public void changeStatus(String status) {
        this.status = status;
    }
    
}
