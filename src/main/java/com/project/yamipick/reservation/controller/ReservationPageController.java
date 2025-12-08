package com.project.yamipick.reservation.controller;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal; // ★ 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.reservation.service.ReservationService;
import com.project.yamipick.reservation.auth.CustomUserDetails; // ★ 네가 만든 클래스 import

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation") // ★ 이거 달면 URL이 "/reservation/..." 로 통일됨
public class ReservationPageController {

    private final ReservationService reservationService;

    // 예약 메인화면
    @GetMapping("/main")
    public String reservationMain() {
        return "reservation/reservationMain"; // reservationMain.html
    }

    // 예약 폼 화면
    @GetMapping("/form")
    public String form(@AuthenticationPrincipal CustomUserDetails loginUser,
                       Model model) {

        if (loginUser == null) {
            return "redirect:/login";  // 로그인 안 되어 있으면 로그인 페이지로
        }

        ReservationDTO dto = new ReservationDTO();

        // ★ 로그인한 유저의 seqUser 사용
        dto.setSeqUser(loginUser.getUser().getSeqUser());

        // 매장/테이블은 일단 하드코딩 유지 (나중에 선택하게 바꾸면 됨)
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

    // 내 예약 목록 화면
    @GetMapping("/list")
    public String myReservationList(@AuthenticationPrincipal CustomUserDetails loginUser,
                                    Model model) {

        if (loginUser == null) {
            return "redirect:/login";  // 로그인 안 한 상태라면 로그인으로 보냄
        }

        Long seqUser = loginUser.getUser().getSeqUser();  // ★ 여기서 진짜 로그인 유저 PK 뽑기

        var reservations = reservationService.getUserReservations(seqUser);
        model.addAttribute("reservations", reservations);

        return "reservation/list"; // templates/reservation/list.html
    }

    // 예약 상세보기
    @GetMapping("/detail/{seqReservation}")
    public String detail(@PathVariable("seqReservation") Long seqReservation,
                         Model model) {

        ReservationDTO reservation = reservationService.getReservationDetail(seqReservation);
        model.addAttribute("reservation", reservation);

        return "reservation/detail";
    }

    // 예약 취소
    @PostMapping("/cancel/{seqReservation}")
    public String cancel(@PathVariable("seqReservation") Long seqReservation,
                         RedirectAttributes rttr) {

        reservationService.cancelReservation(seqReservation);
        rttr.addFlashAttribute("msg", "예약이 취소되었습니다.");

        return "redirect:/reservation/list"; // "/reservation/list" 로 가게 됨 (위 @RequestMapping 덕분)
    }
}
