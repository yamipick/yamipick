package com.project.yamipick.store.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 매장 유저 로그인 후 진입: /store/main
    @GetMapping("/main")
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
    @GetMapping("/dashboard")
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
    @GetMapping("/register")
    public String registerForm() {
        // 여기서 카카오 지도 + 검색 UI 띄우는 화면
        return "store/register";   // templates/store/register.html
    }

    // 매장 등록 처리
    @PostMapping("/register")
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

        try {
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

        } catch (IllegalArgumentException e) {
        	
        	// 영업시간/브레이크 타임 검증 에러 메시지 여기로 떨어짐
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/store/register"; // 다시 등록 화면으로
        }
        
        rttr.addFlashAttribute("msg", "매장 등록 완료");
        return "redirect:/store/dashboard";
       
    }
    
    // 영업시간 수정 폼
    @GetMapping("/schedule/edit")
    public String editSchedule(@AuthenticationPrincipal CustomUserDetails principal,
                               Model model) {

        User loginUser = principal.getUser();
        Store store = storeService.findByUser(loginUser)
                .orElseThrow(() -> new IllegalStateException("매장이 존재하지 않습니다."));
        
        // ★ 여기 한 줄 추가: 스케줄 없으면 자동 생성
        storeService.initSchedulesIfEmpty(store);
        
        List<StoreSchedule> schedules = storeService.getSchedules(store);

        model.addAttribute("store", store);
        model.addAttribute("schedules", schedules);

        return "store/scheduleedit"; // templates/store/schedule-edit.html
    }
    
    @PostMapping("/schedule/edit")
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

        // null 리스트 방어: 길이 맞춰주기
        int size = seqSchedule.size();
        breakStart = normalizeList(breakStart, size);
        breakEnd   = normalizeList(breakEnd, size);
        
        // 서비스에 위임
        storeService.updateSchedules(
                store,
                seqSchedule, isOpen, openTime, closeTime, breakStart, breakEnd
        );

        rttr.addFlashAttribute("msg", "영업시간이 수정되었습니다.");
        return "redirect:/store/dashboard";
    }
    
    private List<String> normalizeList(List<String> list, int size) {
        if (list == null) {
            return java.util.Collections.nCopies(size, null);
        }
        return list;
    }
    
    //테이블 타입 수정 화면
    @GetMapping("/table/edit")
    public String editTableTypes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    	
    	User user = userDetails.getUser();
    	Store store = storeService.findByUser(user)
    			.orElseThrow(() -> new IllegalStateException("매장이 없습니다"));
    	
    	//기존에 등록된 테이블 타입 조회
    	List<StoreTableType> tableTypes = storeService.getTableTypes(store);
    	
    	model.addAttribute("store", store);
    	model.addAttribute("tableTypes", tableTypes);
    	
    	return "store/tableedit";
    }
    
    //테이블 타입 수정 저장
    @PostMapping("/table/edit")
    public String updateTableTypes(@AuthenticationPrincipal CustomUserDetails userDetails,
						            @RequestParam("seqStoreTable") List<Long> seqStoreTable,
						            @RequestParam("name") List<String> name,
						            @RequestParam("capacity") List<Integer> capacity,
						            @RequestParam("quantity") List<Integer> quantity,
						            RedirectAttributes rttr) {

			User user = userDetails.getUser();
			Store store = storeService.findByUser(user)
			.orElseThrow(() -> new IllegalStateException("매장이 없습니다"));
			
			// 기존 테이블만 수정
			storeService.updateTableTypes(store, seqStoreTable, name, capacity, quantity);
			
			rttr.addFlashAttribute("msg", "테이블 정보가 수정되었습니다.");
			return "redirect:/store/dashboard";
	}
    
    //테이블 타입 추가 화면
    @GetMapping("/table/add")
    public String addTableTypesForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {
        User user = userDetails.getUser();
        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        model.addAttribute("store", store);
        return "store/tableadd";
    }
    
    // ✅ 새 테이블 "추가" 전용
    @PostMapping("/table/add")
    public String addTableTypes(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("newTableTypeName") List<String> newTableTypeName,
                                @RequestParam("newTableCapacity") List<Integer> newTableCapacity,
                                @RequestParam("newTableQuantity") List<Integer> newTableQuantity,
                                RedirectAttributes rttr) {

        User user = userDetails.getUser();
        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        // 새 테이블만 추가
        storeService.addTableTypes(store, newTableTypeName, newTableCapacity, newTableQuantity);

        rttr.addFlashAttribute("msg", "새 테이블 타입이 추가되었습니다.");
        return "redirect:/store/dashboard";
    }
    
    //목록에서 테이블 삭제
    @PostMapping("/table/delete/{seqStoreTable}")
    public String deleteTableType(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("seqStoreTable") Long seqStoreTable,
                                  RedirectAttributes rttr) {

        User user = userDetails.getUser();
        Store store = storeService.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("매장이 없습니다"));

        storeService.deleteTableType(store, seqStoreTable);

        rttr.addFlashAttribute("msg", "테이블 타입이 삭제되었습니다.");
        return "redirect:/store/dashboard";
    }
    
}
