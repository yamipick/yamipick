package com.project.yamipick.store.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yamipick.reservation.auth.CustomUserDetails;
import com.project.yamipick.reservation.entity.StoreSchedule;
import com.project.yamipick.reservation.entity.StoreTableType;
import com.project.yamipick.store.entity.Store;
import com.project.yamipick.store.service.StoreService;
import com.project.yamipick.user.entity.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 매장 유저 로그인 후 진입: /store/main
    @GetMapping("/store/main")
    public String storeMain(@AuthenticationPrincipal CustomUserDetails principal) {

        User loginUser = principal.getUser();

        // 매장 유저가 아니면 예약 메인으로
        if (!"STORE".equalsIgnoreCase(loginUser.getRole())) {
            return "redirect:/reservation/main";
        }

        // 매장 없으면 등록 페이지로
        if (!storeService.hasStore(loginUser)) {
            return "redirect:/store/register";
        }

        // 매장 있으면 대시보드로
        return "redirect:/store/dashboard";
    }

    // 매장 대시보드
    @GetMapping("/store/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal,
                            Model model) {

        User loginUser = principal.getUser();

        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

     // ★ 추가: 스케줄 / 테이블 타입 조회
        List<StoreSchedule> schedules = storeService.getSchedules(store);
        List<StoreTableType> tableTypes = storeService.getTableTypes(store);
        
        model.addAttribute("store", store);
        model.addAttribute("schedules", schedules);
        model.addAttribute("tableTypes", tableTypes);
        
        return "store/dashboard";
    }

    // 매장 등록 폼
    @GetMapping("/store/register")
    public String registerForm() {
        // 여기서 카카오 지도 + 검색 UI 띄우는 화면
        return "store/register";   // templates/store/register.html
    }

    // 매장 등록 처리
    @PostMapping("/store/register")
    public String register(@AuthenticationPrincipal CustomUserDetails principal,
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

        User loginUser = principal.getUser();

        storeService.registerStoreWithDetail(
                loginUser,
                kakaoPlaceId,
                name,
                address,
                phone,
                openTime,
                closeTime,
                breakStart,
                breakEnd,
                tableTypeName,
                tableCapacity,
                tableQuantity
        );

        rttr.addFlashAttribute("msg", "매장 등록 완료");
        return "redirect:/store/dashboard";
    }
    
    // 영업시간 수정 폼
    @GetMapping("/store/schedule/edit")
    public String editSchedule(@AuthenticationPrincipal CustomUserDetails principal,
                               Model model) {

        User loginUser = principal.getUser();
        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        List<StoreSchedule> schedules = storeService.getSchedules(store);

        model.addAttribute("store", store);
        model.addAttribute("schedules", schedules);

        return "store/scheduleedit"; // templates/store/schedule-edit.html
    }
    
    @PostMapping("/store/schedule/edit")
    public String updateSchedule(@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestParam("seqSchedule") List<Long> seqSchedule,
                                 @RequestParam("isOpen") List<String> isOpen,
                                 @RequestParam("openTime") List<String> openTime,
                                 @RequestParam("closeTime") List<String> closeTime,
                                 @RequestParam("breakStart") List<String> breakStart,
                                 @RequestParam("breakEnd") List<String> breakEnd,
                                 RedirectAttributes rttr) {

        User loginUser = principal.getUser();
        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));

        // 서비스에 위임
        storeService.updateSchedules(
                store,
                seqSchedule, isOpen, openTime, closeTime, breakStart, breakEnd
        );

        rttr.addFlashAttribute("msg", "영업시간이 수정되었습니다.");
        return "redirect:/store/dashboard";
    }
    
}
