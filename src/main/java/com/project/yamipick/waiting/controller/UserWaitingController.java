package com.project.yamipick.waiting.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.dto.StoreInfoDTO;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.service.WaitingService;
// ✅ 우리가 만든 DTO와 세션 관련 Import 필수!
import com.project.yamipick.waiting.dto.SessionUserDTO;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserWaitingController {

    private final WaitingService waitingService;

    // ✅ [수정됨] 헬퍼 메소드: 세션 DTO 우선 확인 -> 없으면 Auth 확인
    private Long getCurrentUserId(Authentication auth, HttpSession session) {
        
        // 1. 세션에서 통합 DTO 꺼내기 (우리가 저장한 "waitingSession")
        if (session != null) {
            SessionUserDTO dto = (SessionUserDTO) session.getAttribute("waitingSession");
            if (dto != null) {
                return dto.getSeqUser(); // DTO에 있는 유저 번호 리턴
            }
        }

        // 2. 세션 만료 등의 이유로 없으면 인증 객체로 비상 조회
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        
        // (주의: 여기서는 Repository 조회를 안 하고 그냥 이름만 가져옴.
        // 필요하다면 여기서 UserRepository를 통해 seqUser를 조회해야 함.
        // 하지만 정상적인 로그인 흐름이라면 1번에서 무조건 걸립니다.)
        throw new IllegalStateException("세션 정보가 만료되었습니다. 다시 로그인해주세요.");
    }

    // 1. 웨이팅 등록
    @PostMapping("/waiting/register")
    public ResponseEntity<?> register(
            Authentication auth, 
            HttpSession session, // ✅ 세션 파라미터 추가
            @RequestParam("storeId") Long storeId,
            @RequestParam("size") int size
    ) {
        try {
            // ✅ (auth, session) 둘 다 넘김
            Long userId = getCurrentUserId(auth, session); 
            Waiting waiting = waitingService.register(userId, storeId, size);
            return ResponseEntity.ok(waiting);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 내 순서 확인 (로그인 필요 없음, ID로 조회)
    @GetMapping("/waiting/check/{id}")
    public long check(@PathVariable("id") Long id) {
        return waitingService.getCountAhead(id);
    }

    // 3. 취소 (보안 강화 버전)
    @PostMapping("/waiting/cancel/{id}")
    public ResponseEntity<?> cancel(
            @PathVariable("id") Long id, 
            Authentication auth, 
            HttpSession session // ✅ 세션 파라미터 추가
    ) {
        try {
            Long userId = getCurrentUserId(auth, session); // ✅ (auth, session)
            waitingService.cancelByUser(id, userId); // 서비스에 본인확인 요청
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 내 히스토리 조회
    @GetMapping("/waiting/history")
    public List<WaitingDTO> history(
            Authentication auth, 
            HttpSession session // ✅ 세션 파라미터 추가
    ) {
        Long userId = getCurrentUserId(auth, session); // ✅ (auth, session)
        return waitingService.getMyHistory(userId);
    }

    // 5. 내 상태 확인 (폴링용 - 로그인 불필요)
    @GetMapping("/waiting/my-status/{id}")
    public String myStatus(@PathVariable("id") Long id) {
        return waitingService.getMyCurrentStatus(id);
    }

    // 6. 내 활성 웨이팅 조회
    @GetMapping("/waiting/my-active")
    public ResponseEntity<?> getMyActive(
            Authentication auth, 
            HttpSession session // ✅ 세션 파라미터 추가
    ) {  
        Long userId = getCurrentUserId(auth, session); // ✅ (auth, session)
        Waiting active = waitingService.getMyActiveWaiting(userId);
        if (active != null) return ResponseEntity.ok(active);
        return ResponseEntity.noContent().build();
    }

    // 7. 순서 미루기 (보안 강화 버전)
    @PostMapping("/waiting/postpone/{id}")
    public ResponseEntity<?> postpone(
            @PathVariable("id") Long id, 
            Authentication auth, 
            HttpSession session // ✅ 세션 파라미터 추가
    ) {
        try {
            Long userId = getCurrentUserId(auth, session); // ✅ (auth, session)
            waitingService.postpone(id, userId);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. 매장 검색 (로그인 불필요)
    @GetMapping("/waiting/search")
    public List<StoreInfoDTO> searchStores(@RequestParam("keyword") String keyword) {
        return waitingService.searchStores(keyword);
    }
}