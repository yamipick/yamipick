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
@Table(name = "tblStoreSchedule")
public class StoreSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqScheduleGen")
    @SequenceGenerator(
            name = "seqScheduleGen",
            sequenceName = "seqSchedule",   // DB 시퀀스 이름
            allocationSize = 1
    )
    private Long seqSchedule;     // PK

    @Column(nullable = false)
    private Integer dayOfWeek;    // 0=일, 1=월 ... 6=토

    @Column(nullable = false, length = 1)
    private String isOpen;        // 'Y' / 'N'

    @Column(nullable = false, length = 5)
    private String openTime;      // 'HH:MM'

    @Column(nullable = false, length = 5)
    private String closeTime;     // 'HH:MM'

    @Column(length = 5)
    private String breakStart;    // 브레이크 시작 (nullable)

    @Column(length = 5)
    private String breakEnd;      // 브레이크 끝 (nullable)

    // FK: 어느 매장의 스케줄인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQSTORE", nullable = false)
    private Store store;
    
    public void updateSchedule(String isOpen,
					            String openTime,
					            String closeTime,
					            String breakStart,
					            String breakEnd) {
		this.isOpen = isOpen;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.breakStart = (breakStart == null || breakStart.isBlank()) ? null : breakStart;
	    this.breakEnd = (breakEnd == null || breakEnd.isBlank()) ? null : breakEnd;
	}
    
 // 기존 필드들 밑에 추가
    public String getDayOfWeekName() {
        if (dayOfWeek == null) return "";

        switch (dayOfWeek) {
            case 0: return "일";
            case 1: return "월";
            case 2: return "화";
            case 3: return "수";
            case 4: return "목";
            case 5: return "금";
            case 6: return "토";
            default: return "";
        }
    }
}
