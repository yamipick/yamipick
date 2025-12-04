package com.project.yamipick.reservation.model;

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
public class StoreScheduleDTO {
	
	private Long seqSchedule;
	private String dayOfWeek;
	private String isOpen;
	private String openTime;
	private String closeTime;
	private String breakStart;
	private String breakEnd;

}
