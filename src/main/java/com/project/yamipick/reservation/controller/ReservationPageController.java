package com.project.yamipick.reservation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // ✅ 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yamipick.reservation.dto.HolidayDTO;
import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.reservation.repository.StoreTableTypeRepository;
import com.project.yamipick.reservation.service.HolidayService;
import com.project.yamipick.reservation.service.ReservationService;
import com.project.yamipick.store.repository.StoreRepository;
import com.project.yamipick.store.service.StoreService;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationPageController {

    private final UserRepository userRepository;
    private final ReservationService reservationService;
    private final StoreRepository storeRepository;
    private final StoreTableTypeRepository storeTableTypeRepository;
    private final StoreService storeService;
    private final HolidayService holidayService;

    // ✅ 공통: 로그인 유저 조회 (Security 못 건드리므로 username 기반)
    private User getLoginUser(UserDetails principal) {
        if (principal == null) return null;
        String loginId = principal.getUsername();
        return userRepository.findByUserId(loginId).orElse(null);
    }

    // 예약 메인화면
    @GetMapping("/main")
    public String reservationMain(@AuthenticationPrincipal UserDetails principal, Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        Long seqUser = loginUser.getSeqUser();

        var notifications = reservationService.getUserNotificationReservations(seqUser);
        model.addAttribute("notifications", notifications);

        return "reservation/reservationMain";
    }

    // 매장 선택화면
    @GetMapping("/storeSelect")
    public String storeSelect(@AuthenticationPrincipal UserDetails principal,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        var stores = (keyword == null || keyword.isBlank())
                ? storeService.getAllStores()
                : storeService.searchStores(keyword);

        model.addAttribute("stores", stores);
        model.addAttribute("keyword", keyword);

        return "reservation/storeSelect";
    }

    // 예약 폼 화면
    @GetMapping("/form")
    public String form(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam("seqStore") Long seqStore,
                       Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        var store = storeService.findById(seqStore)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다. seqStore=" + seqStore));

        ReservationDTO dto = new ReservationDTO();
        dto.setSeqUser(loginUser.getSeqUser());   // ✅ 변경
        dto.setSeqStore(seqStore);
        dto.setReserveDate(LocalDate.now());

        model.addAttribute("reservation", dto);
        model.addAttribute("store", store);

        return "reservation/form";
    }

    @PostMapping("/formok")
    public String formOk(@AuthenticationPrincipal UserDetails principal, ReservationDTO dto) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        dto.setSeqUser(loginUser.getSeqUser()); // ✅ 변경
        reservationService.createReservation(dto);

        return "redirect:/reservation/complete";
    }

    @GetMapping("/complete")
    public String complete() {
        return "reservation/complete";
    }

    // 내 예약 목록 화면
    @GetMapping("/list")
    public String myReservationList(@AuthenticationPrincipal UserDetails principal, Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        Long seqUser = loginUser.getSeqUser(); // ✅ 변경

        var reservations = reservationService.getUserReservations(seqUser);
        model.addAttribute("reservations", reservations);

        return "reservation/list";
    }

    // 예약 상세보기 (로그인 필요 여부는 니 정책에 따라)
    @GetMapping("/detail/{seqReservation}")
    public String detail(@PathVariable("seqReservation") Long seqReservation, Model model) {

        ReservationDTO reservation = reservationService.getReservationDetail(seqReservation);
        model.addAttribute("reservation", reservation);

        return "reservation/detail";
    }

    // 예약 취소
    @PostMapping("/cancel/{seqReservation}")
    public String cancel(@PathVariable("seqReservation") Long seqReservation,
                         @RequestParam("reason") String reason,
                         RedirectAttributes rttr) {

        reservationService.cancelReservation(seqReservation, reason);
        rttr.addFlashAttribute("msg", "예약이 취소되었습니다.");

        return "redirect:/reservation/list";
    }

    @PostMapping("/tableSelect")
    public String tableSelect(@AuthenticationPrincipal UserDetails principal,
                              @RequestParam("seqStore") Long seqStore,
                              @RequestParam("reserveDate") String reserveDateStr,
                              @RequestParam("reserveTime") String reserveTime,
                              @RequestParam("peopleCount") Integer peopleCount,
                              Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        ReservationDTO dto = new ReservationDTO();
        dto.setSeqUser(loginUser.getSeqUser()); // ✅ 변경
        dto.setSeqStore(seqStore);
        dto.setReserveDate(LocalDate.parse(reserveDateStr));
        dto.setReserveTime(reserveTime);
        dto.setPeopleCount(peopleCount);

        var tableOptions = reservationService.getAvailableTableTypes(
                seqStore, dto.getReserveDate(), dto.getReserveTime(), dto.getPeopleCount()
        );

        model.addAttribute("reservation", dto);
        model.addAttribute("tableOptions", tableOptions);

        return "reservation/tableSelect";
    }

    @PostMapping("/tableSelect/confirm")
    public String tableSelectConfirm(@ModelAttribute("reservation") ReservationDTO dto) {
        reservationService.createReservation(dto);
        return "redirect:/reservation/complete";
    }

    @GetMapping("/available-times")
    @ResponseBody
    public List<ReservationService.TimeSlotDTO> getAvailableTimes(
            @RequestParam("seqStore") Long seqStore,
            @RequestParam("reserveDate") String reserveDate,
            @RequestParam("peopleCount") Integer peopleCount) {

        return reservationService.getAvailableTimeSlots(seqStore, reserveDate, peopleCount);
    }

    @GetMapping("/holiday-dates")
    @ResponseBody
    public List<String> getHolidayDates(
            @RequestParam("seqStore") Long seqStore,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        return reservationService.getHolidayDatesForMonth(seqStore, year, month);
    }

    @GetMapping("/public-holidays")
    @ResponseBody
    public List<HolidayDTO> publicHolidays(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        return holidayService.getHolidays(year, month);
    }
}
