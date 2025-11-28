package com.project.yamipick.log.controller;

import com.project.yamipick.log.entity.BusinessLog;
import com.project.yamipick.log.repository.BusinessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
@Slf4j
public class LogApiController {

    private final BusinessLogRepository logRepository;

    // 프론트에서 { "actionType": "CLICK", "targetId": "강남역" ... } 이렇게 보내면 받음
    @PostMapping("/collect")
    public ResponseEntity<String> collectLog(@RequestBody Map<String, String> data, HttpServletRequest req) {
        
        try {
            // 1. 프론트에서 보낸 데이터 + 백엔드에서 알 수 있는 정보(ID, IP) 합체
            BusinessLog logEntity = BusinessLog.builder()
                    .actionType(data.getOrDefault("actionType", "USER_ACTION")) // 필수
                    .targetType(data.getOrDefault("targetType", "ETC"))
                    .targetId(data.getOrDefault("targetId", ""))
                    .message(data.getOrDefault("message", ""))
                    .userId(getCurrentUserId()) // ★ 로그인한 사람 ID 자동 추출 (없으면 GUEST)
                    .ipAddr(getClientIp(req))   // ★ IP 자동 추출
                    .build();

            // 2. DB 저장
            logRepository.save(logEntity);
            
            return ResponseEntity.ok("LOG SAVED");
            
        } catch (Exception e) {
            log.error("로그 수집 실패", e);
            return ResponseEntity.badRequest().body("FAIL");
        }
    }

    // [유틸] 현재 로그인한 ID 가져오기
    private String getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {}
        return "GUEST"; // 비회원
    }

    // [유틸] IP 주소 가져오기
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        return ip;
    }
}