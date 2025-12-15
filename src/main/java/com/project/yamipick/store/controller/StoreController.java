package com.project.yamipick.store.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // ✅
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.reservation.entity.StoreSchedule;
import com.project.yamipick.reservation.entity.StoreTableType;
import com.project.yamipick.reservation.service.ReservationService;
import com.project.yamipick.store.entity.Store;
import com.project.yamipick.store.service.StoreService;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository; // ✅

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final ReservationService reservationService;
    private final UserRepository userRepository; // ✅ 추가

    private User getLoginUser(UserDetails principal) {
        if (principal == null) return null;
        return userRepository.findByUserId(principal.getUsername()).orElse(null);
    }

    @GetMapping("/main")
    public String storeMain(@AuthenticationPrincipal UserDetails principal) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        // 매장 유저가 아니면 예약 메인으로
        if (!"STORE".equalsIgnoreCase(loginUser.getRole())) {
            return "redirect:/reservation/main";
        }

        if (!storeService.hasStore(loginUser)) {
            return "redirect:/store/register";
        }

        return "redirect:/store/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        List<StoreSchedule> schedules = storeService.getSchedules(store);
        List<StoreTableType> tableTypes = storeService.getTableTypes(store);

        long waitingCount = reservationService.countWaitingReservationsForStore(store.getSeqStore());

        model.addAttribute("store", store);
        model.addAttribute("schedules", schedules);
        model.addAttribute("tableTypes", tableTypes);
        model.addAttribute("waitingCount", waitingCount);

        return "store/dashboard";
    }

    @Value("${yamipick.api.kakao.js-key}")
    private String kakaoJsKey;
    
    @GetMapping("/store/register") // 네 매핑에 맞게
    public String registerForm(Model model) {
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        return "store/register";   // 네 템플릿 경로
    }

    @PostMapping("/register")
    public String register(@AuthenticationPrincipal UserDetails principal,
                           @RequestParam("kakaoPlaceId") String kakaoPlaceId,
                           @RequestParam("name") String name,
                           @RequestParam("address") String address,
                           @RequestParam(value = "phone", required = false) String phone,
                           @RequestParam("openTime") String openTime,
                           @RequestParam("closeTime") String closeTime,
                           @RequestParam(value = "breakStart", required = false) String breakStart,
                           @RequestParam(value = "breakEnd", required = false) String breakEnd,
                           @RequestParam("tableTypeName") List<String> tableTypeName,
                           @RequestParam("tableCapacity") List<Integer> tableCapacity,
                           @RequestParam("tableQuantity") List<Integer> tableQuantity,
                           RedirectAttributes rttr) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        try {
            storeService.registerStoreWithDetail(
                    loginUser, kakaoPlaceId, name, address, phone,
                    openTime, closeTime, breakStart, breakEnd,
                    tableTypeName, tableCapacity, tableQuantity
            );
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/store/register";
        }

        rttr.addFlashAttribute("msg", "매장 등록 완료");
        return "redirect:/store/dashboard";
    }

    @GetMapping("/schedule/edit")
    public String editSchedule(@AuthenticationPrincipal UserDetails principal, Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        storeService.initSchedulesIfEmpty(store);

        List<StoreSchedule> schedules = storeService.getSchedules(store);

        model.addAttribute("store", store);
        model.addAttribute("schedules", schedules);

        return "store/scheduleedit";
    }

    @PostMapping("/schedule/edit")
    public String updateSchedule(@AuthenticationPrincipal UserDetails principal,
                                 @RequestParam("seqSchedule") List<Long> seqSchedule,
                                 @RequestParam("isOpen") List<String> isOpen,
                                 @RequestParam("openTime") List<String> openTime,
                                 @RequestParam("closeTime") List<String> closeTime,
                                 @RequestParam("breakStart") List<String> breakStart,
                                 @RequestParam("breakEnd") List<String> breakEnd,
                                 RedirectAttributes rttr) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        int size = seqSchedule.size();
        breakStart = normalizeList(breakStart, size);
        breakEnd   = normalizeList(breakEnd, size);

        storeService.updateSchedules(store, seqSchedule, isOpen, openTime, closeTime, breakStart, breakEnd);

        rttr.addFlashAttribute("msg", "영업시간이 수정되었습니다.");
        return "redirect:/store/dashboard";
    }

    private List<String> normalizeList(List<String> list, int size) {
        if (list == null) return java.util.Collections.nCopies(size, null);
        return list;
    }

    @GetMapping("/table/edit")
    public String editTableTypes(@AuthenticationPrincipal UserDetails principal, Model model) {

        User user = getLoginUser(principal);
        if (user == null) return "redirect:/login";

        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        List<StoreTableType> tableTypes = storeService.getTableTypes(store);

        model.addAttribute("store", store);
        model.addAttribute("tableTypes", tableTypes);

        return "store/tableedit";
    }

    @PostMapping("/table/edit")
    public String updateTableTypes(@AuthenticationPrincipal UserDetails principal,
                                   @RequestParam("seqStoreTable") List<Long> seqStoreTable,
                                   @RequestParam("name") List<String> name,
                                   @RequestParam("capacity") List<Integer> capacity,
                                   @RequestParam("quantity") List<Integer> quantity,
                                   RedirectAttributes rttr) {

        User user = getLoginUser(principal);
        if (user == null) return "redirect:/login";

        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        storeService.updateTableTypes(store, seqStoreTable, name, capacity, quantity);

        rttr.addFlashAttribute("msg", "테이블 정보가 수정되었습니다.");
        return "redirect:/store/dashboard";
    }

    @GetMapping("/table/add")
    public String addTableTypesForm(@AuthenticationPrincipal UserDetails principal, Model model) {

        User user = getLoginUser(principal);
        if (user == null) return "redirect:/login";

        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        model.addAttribute("store", store);
        return "store/tableadd";
    }

    @PostMapping("/table/add")
    public String addTableTypes(@AuthenticationPrincipal UserDetails principal,
                                @RequestParam("newTableTypeName") List<String> newTableTypeName,
                                @RequestParam("newTableCapacity") List<Integer> newTableCapacity,
                                @RequestParam("newTableQuantity") List<Integer> newTableQuantity,
                                RedirectAttributes rttr) {

        User user = getLoginUser(principal);
        if (user == null) return "redirect:/login";

        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        storeService.addTableTypes(store, newTableTypeName, newTableCapacity, newTableQuantity);

        rttr.addFlashAttribute("msg", "새 테이블 타입이 추가되었습니다.");
        return "redirect:/store/dashboard";
    }

    @PostMapping("/table/delete/{seqStoreTable}")
    public String deleteTableType(@AuthenticationPrincipal UserDetails principal,
                                  @PathVariable("seqStoreTable") Long seqStoreTable,
                                  RedirectAttributes rttr) {

        User user = getLoginUser(principal);
        if (user == null) return "redirect:/login";

        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        storeService.deleteTableType(store, seqStoreTable);

        rttr.addFlashAttribute("msg", "테이블 타입이 삭제되었습니다.");
        return "redirect:/store/dashboard";
    }

    @GetMapping("/reservation/today")
    public String todayReservations(@AuthenticationPrincipal UserDetails principal, Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        LocalDate today = LocalDate.now();

        List<ReservationDTO> reservations =
                reservationService.getStoreReservations(store.getSeqStore(), today);

        model.addAttribute("store", store);
        model.addAttribute("date", today);
        model.addAttribute("reservations", reservations);

        return "store/reservationToday";
    }

    @GetMapping("/reservation/list")
    public String reservationsByDate(@AuthenticationPrincipal UserDetails principal,
                                     @RequestParam(value = "date", required = false) String dateStr,
                                     Model model) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        LocalDate date = (dateStr == null || dateStr.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(dateStr);

        List<ReservationDTO> reservations =
                reservationService.getStoreReservations(store.getSeqStore(), date);

        model.addAttribute("store", store);
        model.addAttribute("date", date);
        model.addAttribute("reservations", reservations);

        return "store/reservationList";
    }

    @PostMapping("/reservation/{seqReservation}/confirm")
    public String confirmReservation(@AuthenticationPrincipal UserDetails principal,
                                     @PathVariable("seqReservation") Long seqReservation,
                                     RedirectAttributes rttr) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        reservationService.confirmReservation(seqReservation);

        rttr.addFlashAttribute("msg", "예약이 확정되었습니다.");
        return "redirect:/store/reservation/today";
    }

    @PostMapping("/reservation/{seqReservation}/cancel")
    public String cancelReservationByStore(@AuthenticationPrincipal UserDetails principal,
                                          @PathVariable("seqReservation") Long seqReservation,
                                          @RequestParam("reason") String reason,
                                          RedirectAttributes rttr) {

        User loginUser = getLoginUser(principal);
        if (loginUser == null) return "redirect:/login";

        storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        reservationService.storeCancelReservation(seqReservation, reason);

        rttr.addFlashAttribute("msg", "예약이 취소되었습니다.");
        return "redirect:/store/reservation/today";
    }
}
