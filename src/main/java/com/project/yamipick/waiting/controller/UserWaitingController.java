package com.project.yamipick.waiting.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.service.WaitingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserWaitingController {

    private final WaitingService waitingService;

    // 1. 등록 (에러 메시지 처리 강화)
    @PostMapping("/waiting/register")
    public ResponseEntity<?> register(
            @RequestParam("userId") Long userId, 
            @RequestParam("storeId") Long storeId, // ★ [추가] 이거 받아야 함!
            @RequestParam("size") int size
    ) {
        try {
            // 서비스로 storeId 토스!
            Waiting waiting = waitingService.register(userId, storeId, size);
            return ResponseEntity.ok(waiting);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }	catch (IllegalArgumentException e) {
            // ★ [추가] 인원수 검증 실패 시 여기로 옴! (400 Bad Request 반환)
            return ResponseEntity.badRequest().body(e.getMessage());  
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류");
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

    // 4. 기록 조회
 // ★ [수정] 리턴 타입 변경 (DTO 사용)
    @GetMapping("/waiting/history")
    public List<WaitingDTO> history(@RequestParam("userId") Long userId) {
        return waitingService.getMyHistory(userId);
    }
    
    // ★ [추가] 내 상태 확인 API (폴링용)
    @GetMapping("/waiting/my-status/{id}")
    public String myStatus(@PathVariable("id") Long id) {
        return waitingService.getMyCurrentStatus(id);
    }
    // ★ [신규] 내 유효한 웨이팅 찾기 (새로고침 해도 연결 유지용)
    @GetMapping("/waiting/my-active")
    public ResponseEntity<?> getMyActive(@RequestParam("userId") Long userId) {
        Waiting active = waitingService.getMyActiveWaiting(userId);
        if (active != null) return ResponseEntity.ok(active);
        return ResponseEntity.noContent().build();
    }
    
 // 순서 미루기
    @PostMapping("/waiting/postpone/{id}")
    public ResponseEntity<?> postpone(@PathVariable("id") Long id) {
        try {
            waitingService.postpone(id);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}