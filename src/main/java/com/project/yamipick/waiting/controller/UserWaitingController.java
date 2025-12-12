package com.project.yamipick.waiting.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;
import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.dto.StoreInfoDTO;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.service.WaitingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserWaitingController {

    private final WaitingService waitingService;
    private final UserRepository userRepository;

    // ✅ 헬퍼 메소드: 현재 로그인한 사용자의 seqUser 가져오기
    private Long getCurrentUserId(Authentication auth) {
    	
    	if (auth != null) {
            System.out.println(">>> auth.getName() = " + auth.getName());
            System.out.println(">>> auth.isAuthenticated() = " + auth.isAuthenticated());
        }
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        String userId = auth.getName(); // 로그인 ID (예: "user01")
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
        return user.getSeqUser();
    }

    // 1. 웨이팅 등록
    @PostMapping("/waiting/register")
    public ResponseEntity<?> register(
            Authentication auth,  // ✅ 세션에서 인증 정보 가져오기
            @RequestParam("storeId") Long storeId,
            @RequestParam("size") int size
    ) {
        try {
            Long userId = getCurrentUserId(auth);  // ✅ 세션에서 userId 추출
            Waiting waiting = waitingService.register(userId, storeId, size);
            return ResponseEntity.ok(waiting);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }

    // 2. 내 순서 확인
    @GetMapping("/waiting/check/{id}")
    public long check(@PathVariable("id") Long id) {
        return waitingService.getCountAhead(id);
    }

    // 3. 취소
    @PostMapping("/waiting/cancel/{id}")
    public String cancel(@PathVariable("id") Long id) {
        waitingService.cancel(id, false);
        return "ok";
    }

    // 4. 내 히스토리 조회
    @GetMapping("/waiting/history")
    public List<WaitingDTO> history(Authentication auth) {  // ✅ 세션 사용
        Long userId = getCurrentUserId(auth);
        return waitingService.getMyHistory(userId);
    }

    // 5. 내 상태 확인 (폴링용)
    @GetMapping("/waiting/my-status/{id}")
    public String myStatus(@PathVariable("id") Long id) {
        return waitingService.getMyCurrentStatus(id);
    }

    // 6. 내 활성 웨이팅 조회
    @GetMapping("/waiting/my-active")
    public ResponseEntity<?> getMyActive(Authentication auth) {  // ✅ 세션 사용
        Long userId = getCurrentUserId(auth);
        Waiting active = waitingService.getMyActiveWaiting(userId);
        if (active != null) return ResponseEntity.ok(active);
        return ResponseEntity.noContent().build();
    }

    // 7. 순서 미루기
    @PostMapping("/waiting/postpone/{id}")
    public ResponseEntity<?> postpone(@PathVariable("id") Long id) {
        try {
            waitingService.postpone(id);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. 매장 검색
    @GetMapping("/waiting/search")
    public List<StoreInfoDTO> searchStores(@RequestParam("keyword") String keyword) {
        return waitingService.searchStores(keyword);
    }
}