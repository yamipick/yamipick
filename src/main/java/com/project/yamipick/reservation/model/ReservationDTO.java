package com.project.yamipick.reservation.model;

import java.util.Date;

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
	private Date reserveDate;
	private String reserveTime;
	private Integer peopleCount;
	private String status;
	
}
