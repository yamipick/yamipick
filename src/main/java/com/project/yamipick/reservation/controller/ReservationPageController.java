package com.project.yamipick.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.service.ReservationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationPageController {

	private final ReservationService reservationService;
	
	//예약 폼 화면
	@GetMapping("/form")
	public String form(Model model) {
		
		ReservationDTO dto = new ReservationDTO();
		
		dto.setSeqUser(1L);
		dto.setSeqStore(6L);
		dto.setSeqStoreTable(3L);
		
		model.addAttribute("reservation", dto);
		
		return "reservation/form";
		
	}
	
	@PostMapping("/formok")
	public String formOk(ReservationDTO dto) {
		
		reservationService.createReservation(dto);
		
		return "redirect:/reservation/complete";
	}
	
	@GetMapping("/complete")
	public String complete() {
		
		return "reservation/complete";
	}
	
}
