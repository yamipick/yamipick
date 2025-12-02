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
    public ResponseEntity<?> register(@RequestParam("userId") Long userId, @RequestParam("size") int size) {
        try {
            Waiting waiting = waitingService.register(userId, size);
            return ResponseEntity.ok(waiting); // 성공 시 JSON 반환
        } catch (IllegalStateException e) {
            // ★ 실패 시(마감 등) 에러 메시지를 문자열로 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
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
}