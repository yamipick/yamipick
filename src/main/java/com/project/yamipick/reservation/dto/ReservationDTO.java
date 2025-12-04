package com.project.yamipick.reservation.dto;

import java.time.LocalDate;

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
	private LocalDate reserveDate;
	private String reserveTime;
	private Integer peopleCount;
	private String status;
	
	private Long seqUser;
	private Long seqStore;
	private Long seqStoreTable;
	
}
