package com.project.yamipick.user.scheduler;

import com.project.yamipick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserStatusScheduler {

    private final UserRepository userRepository;

    // 매일 자정(0시 0분 0초)마다 실행
    @Scheduled(cron = "0 0 0 * * *") 
    @Transactional
    public void releaseSuspendedUsers() {
        log.info("정지 해제 스케줄러 실행됨 - {}", LocalDateTime.now());

        // 정지 상태('SUSPENDED')이면서, 해제 날짜(suspendedUntil)가 현재 시간보다 과거인 유저 찾기
        // (이 로직은 Repository에 쿼리 메소드를 추가해서 한 방에 업데이트하는 게 효율적입니다)
        
        int updatedCount = userRepository.updateStatusToActiveIfSuspensionEnded(LocalDateTime.now());
        
        if (updatedCount > 0) {
            log.info("총 {}명의 정지 회원이 활동 상태로 복구되었습니다.", updatedCount);
        }
    }
}