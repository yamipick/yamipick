package com.project.yamipick.log.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

// ★ Security 관련 임포트 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.yamipick.log.repository.BusinessLogRepository;
import com.project.yamipick.log.entity.BusinessLog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    private BusinessLogRepository logRepository;

    // 1. [정상 수행]
    @AfterReturning(pointcut = "execution(* com.project.yamipick..*Controller.*(..))", returning = "result")
    public void captureLog(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, "SUCCESS");
    }

    // 2. [에러 발생]
    @AfterThrowing(pointcut = "execution(* com.project.yamipick..*Controller.*(..))", throwing = "ex")
    public void captureError(JoinPoint joinPoint, Exception ex) {
        handleLog(joinPoint, ex, "ERROR");
    }

    private void handleLog(JoinPoint joinPoint, Exception ex, String status) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 에러거나 중요한 행동이면 기록
        if ("ERROR".equals(status) || isImportantAction(methodName)) {
            
            String params = Arrays.toString(joinPoint.getArgs());
            String message = methodName + " 실행됨. (파라미터: " + params + ")";
            String actionType = "ERROR".equals(status) ? "SYSTEM_ERROR" : "AUTO_LOG";

            if (ex != null) {
                message += "\n[에러] " + ex.getMessage();
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                message += "\n[위치] " + sw.toString();
            }

            BusinessLog logEntity = BusinessLog.builder()
                    .actionType(actionType)
                    .targetType(className)
                    .targetId(status)
                    .userId(getCurrentUserId()) // ★ 핵심: 자동으로 ID 가져오기!
                    .message(message)
                    .ipAddr(getClientIp())
                    .build();

            try {
                logRepository.save(logEntity);
                log.info("✅ 로그 저장 완료 [User: {}]: {}", logEntity.getUserId(), methodName);
            } catch (Exception e) {
                log.error("❌ 로그 저장 실패", e);
            }
        }
    }

    // ★ [핵심] 로그인한 유저 ID 가져오는 메소드
    private String getCurrentUserId() {
        try {
            // 1. 시큐리티 컨텍스트에서 인증 정보 꺼냄
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // 2. 로그인 상태인지 확인
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName(); // 로그인한 ID (예: admin, user1) 반환
            }
        } catch (Exception e) {
            // 무시
        }
        return "GUEST"; // 비회원이면 GUEST로 기록
    }

    // IP 가져오기 (기존 동일)
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
                return ip;
            }
        } catch (Exception e) {}
        return "0.0.0.0";
    }

    // 중요 행동 필터 (기존 동일)
    private boolean isImportantAction(String name) {
        String lower = name.toLowerCase();
        return lower.startsWith("add") || lower.startsWith("save") || 
               lower.startsWith("create") || lower.startsWith("update") || 
               lower.startsWith("modify") || lower.startsWith("delete") || 
               lower.startsWith("remove") || lower.startsWith("login") || 
               lower.startsWith("logout") || lower.startsWith("confirm") || 
               lower.startsWith("cancel") || lower.startsWith("search") || 
               lower.startsWith("view");
    }
}