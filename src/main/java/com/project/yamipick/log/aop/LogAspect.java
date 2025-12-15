package com.project.yamipick.log.aop;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
// ★ Security 관련 임포트 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.project.yamipick.log.entity.BusinessLog;
import com.project.yamipick.log.repository.BusinessLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;


@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    private BusinessLogRepository logRepository;

    // =========================================================================
    // 1. [변경] "컨트롤러"의 중요한 행동(쓰기/수정/삭제) 감시
    // =========================================================================
    // 설명: Service 대신 Controller 패키지를 바라봅니다.
    @AfterReturning(pointcut = "execution(* com.project.yamipick..controller.*Controller.*(..))", returning = "result")
    public void captureControllerLog(JoinPoint joinPoint, Object result) {
        
        String className = joinPoint.getTarget().getClass().getSimpleName(); // 클래스명 (예: NoticeController)
        String methodName = joinPoint.getSignature().getName();              // 메서드명 (예: write)

        // ---------------------------------------------------------
        // ★ [필터링] 시끄러운 로그 제외 (AI, 채팅, 단순 페이지 이동)
        // ---------------------------------------------------------
        
        // 1. AI, 채팅 관련 컨트롤러나 메서드는 무시
        if (className.contains("Chat") || className.contains("AI") || methodName.contains("Chat")) {
            return; 
        }

        // 2. MapController의 'map' 메서드(검색)는 아래 2번 로직에서 따로 처리하므로 여기서 무시
        if (className.equals("MapController") && methodName.equals("map")) {
            return;
        }

        // 3. 단순 페이지 이동(Form)이나 조회(list, view)는 제외하고, "진짜 행동"만 기록
        // (write, save, add, update, edit, delete, remove 로 시작하는 것만)
        if (isImportantAction(methodName)) {
            handleLog(joinPoint, null, "ACTION");
        }
    }

    // =========================================================================
    // 2. [유지] 지도 검색어 감시 (MapController 전용)
    // =========================================================================
    @AfterReturning(pointcut = "execution(* com.project.yamipick.map.controller.MapController.map(..))", returning = "result")
    public void captureSearchLog(JoinPoint joinPoint, Object result) {
        
        Object[] args = joinPoint.getArgs();
        String keyword = null;

        for (Object arg : args) {
            if (arg instanceof String) {
                String strArg = (String) arg;
                // 유효한 검색어만 추출
                if (!strArg.isEmpty() && !"null".equals(strArg) && !"맛집".equals(strArg)) {
                    keyword = strArg;
                    break; 
                }
            }
        }

        if (keyword != null) {
            saveLog("SEARCH", "KEYWORD", keyword, "'" + keyword + "' 검색됨");
        }
    }

    // =========================================================================
    // 3. [변경] 에러 감시도 "컨트롤러" 기준으로 변경
    // =========================================================================
    @AfterThrowing(pointcut = "execution(* com.project.yamipick..controller.*Controller.*(..))", throwing = "exception")
    public void captureError(JoinPoint joinPoint, Exception exception) {
        // 에러는 AI/채팅이라도 잡는 게 좋으므로 필터링 없이 기록
        handleLog(joinPoint, exception, "ERROR");
    }


    // ---------------------- 내부 로직 (헬퍼 메서드) ----------------------

    private void handleLog(JoinPoint joinPoint, Exception exception, String type) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            String actionType = extractActionType(methodName);
            String message = className + "." + methodName + " 실행됨"; // 예: AdminNoticeController.write 실행됨
            
            if (exception != null) {
                message += " [ERROR] " + exception.getMessage();
                actionType = "ERROR";
            }

            saveLog(actionType, "SYSTEM", methodName, message);
        } catch (Exception e) {
            log.error("로그 저장 실패", e);
        }
    }

    private void saveLog(String actionType, String targetType, String targetId, String message) {
        try {
            String userId = getCurrentUserId();
            String ip = getClientIp();
            
            // ★ [추가] 안전한 저장을 위한 글자 수 자르기 (DB 컬럼 크기 초과 방지)
            // message가 250자를 넘으면 뒤를 자르고 "..."을 붙임
            if (message != null && message.length() > 250) {
                message = message.substring(0, 247) + "...";
            }
            
            logRepository.save(BusinessLog.builder()
                    .actionType(actionType)
                    .targetType(targetType)
                    .targetId(targetId)
                    .userId(userId)
                    .ipAddr(ip)
                    .message(message)
                    .build());
        } catch (Exception e) {
            // 그래도 에러 나면 콘솔에만 찍고 넘어감 (시스템 멈춤 방지)
            log.error("로그 저장 중 오류 발생 (무시됨): {}", e.getMessage());
        }
    }

    private String extractActionType(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.startsWith("login")) return "LOGIN";
        if (lower.startsWith("save") || lower.startsWith("create") || lower.startsWith("add") || lower.startsWith("write")) return "CREATE";
        if (lower.startsWith("update") || lower.startsWith("modify") || lower.startsWith("edit")) return "UPDATE";
        if (lower.startsWith("delete") || lower.startsWith("remove")) return "DELETE";
        return "ACTION";
    }

    // ★ 중요한 행동인지 판별 (단순 조회/폼 이동 제외)
    private boolean isImportantAction(String name) {
        String lower = name.toLowerCase();
        
        // "Form"으로 끝나는 건 보통 페이지 이동이므로 제외 (예: writeForm)
        if (lower.endsWith("form")) return false;

        return lower.startsWith("save") || lower.startsWith("create") || lower.startsWith("add") || lower.startsWith("write") ||
               lower.startsWith("update") || lower.startsWith("modify") || lower.startsWith("edit") ||
               lower.startsWith("delete") || lower.startsWith("remove");
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