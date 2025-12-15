package com.project.yamipick.waiting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.waiting.domain.WaitingLog;
import com.project.yamipick.waiting.repository.WaitingLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WaitingLogService {
    
    private final WaitingLogRepository logRepository;

    // ★ [핵심] 부모 트랜잭션이 롤백되어도 이 로그 저장은 성공함 (독립된 트랜잭션)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(Long storeId, Long waitingId, String type, String msg) {
        logRepository.save(WaitingLog.builder()
                .storeId(storeId)
                .waitingId(waitingId)
                .actionType(type)
                .logMessage(msg)
                .build());
    }
}