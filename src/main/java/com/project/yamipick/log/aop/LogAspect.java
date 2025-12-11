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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.yamipick.log.repository.BusinessLogRepository;
import com.project.yamipick.log.entity.BusinessLog;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    private BusinessLogRepository logRepository;

    // 1. [기본] Service의 중요 메서드 감시 (회원가입, 수정 등)
    @AfterReturning(pointcut = "execution(* com.project.yamipick..*Service.*(..))", returning = "result")
    public void captureServiceLog(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        if (isImportantAction(methodName)) {
            handleLog(joinPoint, null, "SERVICE");
        }
    }

    // 2. ★ [추가] MapController의 검색 감시 (팀원 코드 안 건드리고 여기서 낚아채기!)
    @AfterReturning(pointcut = "execution(* com.project.yamipick.map.controller.MapController.map(..))", returning = "result")
    public void captureSearchLog(JoinPoint joinPoint, Object result) {
        
        // 파라미터 뒤져서 검색어 찾기
        Object[] args = joinPoint.getArgs();
        String keyword = null;

        // MapController.map 메서드의 파라미터 순서를 알 수 없으므로, String 타입이고 값이 있는 걸 찾음
        for (Object arg : args) {
            if (arg instanceof String) {
                String strArg = (String) arg;
                // "맛집" 같은 기본값이나 "null" 문자열 제외하고 유효한 검색어만
                if (!strArg.isEmpty() && !"null".equals(strArg) && !"맛집".equals(strArg)) {
                    keyword = strArg;
                    break; 
                }
            }
        }

        // 유효한 검색어가 있을 때만 저장
        if (keyword != null) {
            saveLog("SEARCH", "KEYWORD", keyword, keyword + "검색됨");
        }
    }

    // 3. [공통] 에러 발생 시 로그
    @AfterThrowing(pointcut = "execution(* com.project.yamipick..*Service.*(..))", throwing = "exception")
    public void captureError(JoinPoint joinPoint, Exception exception) {
        handleLog(joinPoint, exception, "ERROR");
    }

    // 로그 처리 내부 로직
    private void handleLog(JoinPoint joinPoint, Exception exception, String type) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String actionType = extractActionType(methodName);
            String message = "Service: " + methodName;
            
            if (exception != null) message += " [ERROR] " + exception.getMessage();

            saveLog(actionType, "SYSTEM", methodName, message);
        } catch (Exception e) {
            log.error("로그 저장 실패", e);
        }
    }

    // ★ 진짜 저장하는 함수
    private void saveLog(String actionType, String targetType, String targetId, String message) {
        try {
            String userId = getCurrentUserId();
            String ip = getClientIp();

            logRepository.save(BusinessLog.builder()
                    .actionType(actionType) // SEARCH, UPDATE ...
                    .targetType(targetType) // KEYWORD, SYSTEM ...
                    .targetId(targetId)     // 검색어, 메서드명
                    .userId(userId)
                    .ipAddr(ip)
                    .message(message)
                    .build());
        } catch (Exception e) {
            log.error("DB 저장 실패", e);
        }
    }

    // (이하 헬퍼 메서드들은 기존과 동일)
    private String extractActionType(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.startsWith("save") || lower.startsWith("create") || lower.startsWith("add")) return "CREATE";
        if (lower.startsWith("update") || lower.startsWith("modify")) return "UPDATE";
        if (lower.startsWith("delete") || lower.startsWith("remove")) return "DELETE";
        if (lower.startsWith("login")) return "LOGIN";
        return "SERVICE";
    }

    private boolean isImportantAction(String name) {
        String lower = name.toLowerCase();
        return lower.startsWith("save") || lower.startsWith("create") || lower.startsWith("add") ||
               lower.startsWith("update") || lower.startsWith("modify") || 
               lower.startsWith("delete") || lower.startsWith("remove") || lower.startsWith("process");
    }

    private String getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {}
        return "GUEST";
    }

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
}