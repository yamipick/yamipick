package com.project.yamipick.reservation.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.reservation.entity.Reservation;
import com.project.yamipick.reservation.service.ReservationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationPageController {

	private final ReservationService reservationService;
	
	//예약 메인화면
	@GetMapping("/main")
    public String reservationMain() {
        return "reservation/reservationMain"; // reservationMain.html
    }
	
	//예약 폼 화면
	@GetMapping("/form")
	public String form(Model model) {
		
		ReservationDTO dto = new ReservationDTO();
		
		dto.setSeqUser(8L);
		dto.setSeqStore(6L);
		dto.setSeqStoreTable(3L);
		
		dto.setReserveDate(LocalDate.now());
		
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
	
	//내 예약 목록 화면
	@GetMapping("/list")
    public String myReservationList(
            @RequestParam(name = "seqUser", required = false) Long seqUser,
            Model model
    ) {
        // 로그인 붙기 전이니까, 없으면 일단 8번 유저로 가정
        if (seqUser == null) {
            seqUser = 8L;
        }

        var reservations = reservationService.getUserReservations(seqUser);
        model.addAttribute("reservations", reservations);

        return "reservation/list"; // templates/reservation/list.html
    }
	
	//예약 상세보기
	@GetMapping("/detail/{seqReservation}")
	public String detail(@PathVariable("seqReservation") Long seqReservation, Model model) {

	    ReservationDTO reservation = reservationService.getReservationDetail(seqReservation);

	    model.addAttribute("reservation", reservation);
	    return "reservation/detail";
	}
	
	//예약 취소
	@PostMapping("/cancel/{seqReservation}")
    public String cancel(@PathVariable("seqReservation") Long seqReservation, RedirectAttributes rttr) {
        reservationService.cancelReservation(seqReservation);
        rttr.addFlashAttribute("msg", "예약이 취소되었습니다.");
        return "redirect:/reservation/list";              // 목록으로
    }
	
}
