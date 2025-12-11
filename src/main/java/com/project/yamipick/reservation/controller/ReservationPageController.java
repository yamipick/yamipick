package com.project.yamipick.reservation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal; // ★ 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yamipick.reservation.auth.CustomUserDetails; // ★ 네가 만든 클래스 import
import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.reservation.repository.StoreTableTypeRepository;
import com.project.yamipick.reservation.service.ReservationService;
import com.project.yamipick.store.repository.StoreRepository;
import com.project.yamipick.store.service.StoreService;
import com.project.yamipick.user.entity.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation") // ★ 이거 달면 URL이 "/reservation/..." 로 통일됨
public class ReservationPageController {

	private final ReservationService reservationService;
	private final StoreRepository storeRepository;
	private final StoreTableTypeRepository storeTableTypeRepository;
	private final StoreService storeService;

	// 예약 메인화면
	@GetMapping("/main")
	public String reservationMain(@AuthenticationPrincipal CustomUserDetails principal,
            Model model) {
		
		// 로그인 유저
        User loginUser = principal.getUser();
        Long seqUser = loginUser.getSeqUser();

        // ✅ 알림용 예약 리스트 (대기 아닌 것들)
        List<ReservationDTO> notifications =
                reservationService.getUserNotificationReservations(seqUser);

        model.addAttribute("notifications", notifications);
		
		return "reservation/reservationMain"; // reservationMain.html
	}

	// 매장 선택화면
	@GetMapping("/storeSelect")
	public String storeSelect(@AuthenticationPrincipal CustomUserDetails loginUser,
			@org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
			Model model) {

		if (loginUser == null) {
			return "redirect:/login";
		}

		// 키워드 없으면 전체, 있으면 검색
		var stores = (keyword == null || keyword.isBlank()) ? storeService.getAllStores()
				: storeService.searchStores(keyword);

		model.addAttribute("stores", stores);
		model.addAttribute("keyword", keyword);

		return "reservation/storeSelect"; // 새로 만들 템플릿
	}

	// 예약 폼 화면
	@GetMapping("/form")
	public String form(@AuthenticationPrincipal CustomUserDetails loginUser, @RequestParam("seqStore") Long seqStore,
			Model model) {

		if (loginUser == null) {
			return "redirect:/login"; // 로그인 안 되어 있으면 로그인 페이지로
		}

		// 선택한 매장 정보도 같이 보여주고 싶으면
		var store = storeService.findById(seqStore)
				.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다. seqStore=" + seqStore));

		ReservationDTO dto = new ReservationDTO();
		dto.setSeqUser(loginUser.getUser().getSeqUser());
		dto.setSeqStore(seqStore); // ★ 하드코딩 제거, 선택한 매장으로
		// dto.setSeqStoreTable는 나중에 폼에서 선택하게 할 거면 여기선 안 채워도 됨

		dto.setReserveDate(LocalDate.now());

		model.addAttribute("reservation", dto);
		model.addAttribute("store", store);

		return "reservation/form";
	}

	@PostMapping("/formok")
	public String formOk(@AuthenticationPrincipal CustomUserDetails loginUser, ReservationDTO dto) {

		dto.setSeqUser(loginUser.getUser().getSeqUser());
		reservationService.createReservation(dto);

		return "redirect:/reservation/complete";
	}

	@GetMapping("/complete")
	public String complete() {
		return "reservation/complete";
	}

	// 내 예약 목록 화면
	@GetMapping("/list")
	public String myReservationList(@AuthenticationPrincipal CustomUserDetails loginUser, Model model) {

		if (loginUser == null) {
			return "redirect:/login"; // 로그인 안 한 상태라면 로그인으로 보냄
		}

		Long seqUser = loginUser.getUser().getSeqUser(); // ★ 여기서 진짜 로그인 유저 PK 뽑기

		var reservations = reservationService.getUserReservations(seqUser);
		model.addAttribute("reservations", reservations);

		return "reservation/list"; // templates/reservation/list.html
	}

	// 예약 상세보기
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

		return "redirect:/reservation/list"; // "/reservation/list" 로 가게 됨 (위 @RequestMapping 덕분)
	}

	/**
	 * 1단계(form.html)에서 날짜/시간/인원 입력 후 → 2단계(테이블 타입 선택) 화면으로 이동
	 */
	@PostMapping("/tableSelect")
	public String tableSelect(@AuthenticationPrincipal CustomUserDetails loginUser,
			@RequestParam("seqStore") Long seqStore, @RequestParam("reserveDate") String reserveDateStr,
			@RequestParam("reserveTime") String reserveTime, @RequestParam("peopleCount") Integer peopleCount,
			Model model) {

		if (loginUser == null) {
			return "redirect:/login";
		}

		// 1) 예약 DTO 구성 (2단계 화면에서 그대로 다시 제출할 값들)
		ReservationDTO dto = new ReservationDTO();
		dto.setSeqUser(loginUser.getUser().getSeqUser());
		dto.setSeqStore(seqStore);
		dto.setReserveDate(LocalDate.parse(reserveDateStr)); // yyyy-MM-dd 형식
		dto.setReserveTime(reserveTime);
		dto.setPeopleCount(peopleCount);

		// 2) 테이블 타입 목록 조회 (지금은 전부 available=true)
		var tableOptions = reservationService.getAvailableTableTypes(seqStore, dto.getReserveDate(),
				dto.getReserveTime(), dto.getPeopleCount());

		// 3) (선택) 매장 이름 보여주고 싶으면 필요
		// store 선택 페이지에서 이미 모델에 실어줬으면 그대로 쓰고,
		// 여기서는 일단 생략해도 로직에는 문제 없음

		model.addAttribute("reservation", dto);
		model.addAttribute("tableOptions", tableOptions);

		return "reservation/tableSelect"; // 새로 만들 화면
	}

	// 2단계 : 테이블 타입까지 선택 후 실제 예약 생성
	@PostMapping("/tableSelect/confirm")
	public String tableSelectConfirm(@ModelAttribute("reservation") ReservationDTO dto) {

		// 여기서 seqStoreTable 까지 채워진 상태여야 함
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
	
	//휴무일 비활성화
	@GetMapping("/holiday-dates")
	@ResponseBody
	public List<String> getHolidayDates(
			@RequestParam("seqStore") Long seqStore,
	        @RequestParam("year") int year,
	        @RequestParam("month") int month) {

		return reservationService.getHolidayDatesForMonth(seqStore, year, month);
	}

}
