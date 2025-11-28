package com.project.yamipick.log.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

// ★ 중요: 아까 만든 Log 전용 Mapper를 가져옵니다.
import com.project.yamipick.log.mapper.LogMapper; 
import com.project.yamipick.log.dto.LogDTO;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired(required = false) // 혹시 Mapper 설정 실수해도 서버는 켜지게 함
    private LogMapper logMapper;

    // 감시 대상: com.project.yamipick 패키지 아래의 모든 Controller
    @AfterReturning(pointcut = "execution(* com.project.yamipick..*Controller.*(..))", returning = "result")
    public void captureLog(JoinPoint joinPoint, Object result) {
        
        // 1. 실행된 메소드 정보 가져오기
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 2. 중요한 행동만 기록 (조회같은 건 너무 많으니까 제외)
        if (isImportantAction(methodName)) {
            
            // ★ 핵심: Builder 패턴 사용! (세터보다 훨씬 읽기 편함)
            LogDTO logDto = LogDTO.builder()
                    .actionType("AUTO_LOG")
                    .targetType(className)     // 예: AdminController
                    .targetId("SYSTEM")        // 타겟 ID는 상황에 따라 변경 가능
                    .userId("SYSTEM_USER")     // 나중에 세션에서 꺼내오기로 변경 예정
                    .message(methodName + " 메소드가 실행되었습니다.")
                    .ipAddr(getClientIp())     // IP 주소 자동 추출
                    .build();

            // 3. DB 저장
            try {
                if (logMapper != null) {
                    logMapper.insertLog(logDto);
                    log.info("✅ 자동 로그 저장 완료: {} - {}", className, methodName);
                } else {
                    log.warn("⚠️ LogMapper가 없습니다. DB 저장을 건너뜁니다.");
                }
            } catch (Exception e) {
                log.error("❌ 로그 저장 중 에러 발생", e);
            }
        }
    }

    // IP 주소 가져오는 유틸 메소드
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // 무시
        }
        return "0.0.0.0";
    }

    // 중요한 메소드인지 판단 (필터링)
    private boolean isImportantAction(String name) {
        String lower = name.toLowerCase();
        // 이 단어로 시작하는 메소드만 로그를 남김
        return lower.startsWith("add") || 
               lower.startsWith("save") || 
               lower.startsWith("create") || 
               lower.startsWith("update") || 
               lower.startsWith("modify") || 
               lower.startsWith("delete") || 
               lower.startsWith("remove") || 
               lower.startsWith("login") || 
               lower.startsWith("logout") ||
               lower.startsWith("confirm") ||
               lower.startsWith("cancel");
    }
}