package com.project.yamipick.reservation.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
	
	private Long seqReservation;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reserveDate;
	private String reserveTime;
	private Integer peopleCount;
	private String status;
	private String cancelReason;
	private String nickname;
	
	private Long seqUser;
	private Long seqStore;
	private Long seqStoreTable;
	
	private String storeName;
	private String tableTypeName;
	
}
