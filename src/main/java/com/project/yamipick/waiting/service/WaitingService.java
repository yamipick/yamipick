package com.project.yamipick.waiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.domain.WaitingLog;
import com.project.yamipick.waiting.domain.WaitingMember;
import com.project.yamipick.waiting.domain.WaitingNotice;
import com.project.yamipick.waiting.domain.WaitingOperation;
import com.project.yamipick.waiting.domain.WaitingStatus;
import com.project.yamipick.waiting.domain.WaitingStatusType;
import com.project.yamipick.waiting.domain.WaitingStore;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.dto.WaitingNoticeDTO;
import com.project.yamipick.waiting.handler.WaitingWebSocketHandler;
import com.project.yamipick.waiting.repository.WaitingLogRepository;
import com.project.yamipick.waiting.repository.WaitingMemberRepository;
import com.project.yamipick.waiting.repository.WaitingNoticeRepository;
import com.project.yamipick.waiting.repository.WaitingOperationRepository;
import com.project.yamipick.waiting.repository.WaitingRepository;
import com.project.yamipick.waiting.repository.WaitingStatusRepository;
import com.project.yamipick.waiting.repository.WaitingStoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final WaitingMemberRepository memberRepository;
    private final WaitingStoreRepository storeRepository;
    private final WaitingStatusRepository statusRepository;
    private final WaitingOperationRepository operationRepository;
    private final WaitingNoticeRepository noticeRepository;
    private final WaitingLogRepository logRepository;
    private final WaitingWebSocketHandler webSocketHandler;

    // ================================================================================
    // 🔒 내부 헬퍼 메서드: 로그 저장
    // ================================================================================
    private void saveLog(Long storeId, Long waitingId, String type, String msg) {
        logRepository.save(WaitingLog.builder()
                .storeId(storeId)
                .waitingId(waitingId)
                .actionType(type)
                .logMessage(msg)
                .build());
    }

    // ================================================================================
    // ★ [신규] 날짜별 운영 기록 조회 (인원수 포함)
    // ================================================================================
    @Transactional(readOnly = true)
    public Map<String, Object> getDailyReport(Long storeId, String dateStr) {
        // 1. 날짜 파싱 (yyyy-MM-dd)
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 2. 해당 날짜의 로그 조회
        // (주의: Repository에 해당 메서드가 선언되어 있어야 함)
        List<WaitingLog> logs = logRepository.findAllByStoreIdAndRegDateBetweenOrderByRegDateDesc(storeId, start, end);

        // 3. 로그에서 waitingId 추출 (null 제외)
        Set<Long> waitingIds = logs.stream()
                .map(WaitingLog::getWaitingId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 4. 인원수 정보 일괄 조회 (waitingId -> teamSize)
        Map<Long, Integer> sizeMap = new HashMap<>();
        if (!waitingIds.isEmpty()) {
            sizeMap = waitingRepository.findAllById(waitingIds).stream()
                    .collect(Collectors.toMap(Waiting::getId, Waiting::getTeamSize));
        }
        
        // 5. 로그 + 인원수 데이터 합치기
        final Map<Long, Integer> finalSizeMap = sizeMap;
        List<Map<String, Object>> resultList = logs.stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("regDate", log.getRegDate());
            map.put("actionType", log.getActionType());
            map.put("logMessage", log.getLogMessage());
            
            // waitingId가 있고, 조회된 인원수가 있으면 넣고, 아니면 0
            Integer size = finalSizeMap.getOrDefault(log.getWaitingId(), 0);
            map.put("teamSize", size);
            
            return map;
        }).collect(Collectors.toList());

        // 6. 통계 계산
        long totalCalls = logs.stream().filter(l -> "CALL".equals(l.getActionType())).count();
        long totalEnters = logs.stream().filter(l -> "ENTER".equals(l.getActionType())).count();
        long totalCancels = logs.stream().filter(l -> "CANCEL".equals(l.getActionType())).count();

        return Map.of(
            "logs", resultList,
            "stats", Map.of(
                "calls", totalCalls,
                "enters", totalEnters,
                "cancels", totalCancels
            )
        );
    }

    // ================================================================================
    // 1. 웨이팅 등록 및 운영
    // ================================================================================
    public Waiting register(Long userId, Long storeId, int size) {
        LocalDate today = LocalDate.now();
        WaitingOperation op = operationRepository.findByStoreIdAndOperationDate(storeId, today)
                .orElseGet(() -> {
                    WaitingStore store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new IllegalArgumentException("매장 정보 없음"));
                    
                    return operationRepository.save(WaitingOperation.builder()
                            .store(store)
                            .operationDate(today)
                            .status("OPEN")
                            .lastWaitingNum(0)
                            .build());
                });

        if (!"OPEN".equals(op.getStatus())) {
            throw new IllegalStateException("⛔ 현재 웨이팅 접수가 마감되었습니다.");
        }

        int nextNum = op.getLastWaitingNum() + 1;
        op.setLastWaitingNum(nextNum);

        WaitingMember member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));
        
        WaitingStatus initStatus = statusRepository.findByStatusName("WAITING")
                .orElseThrow(() -> new IllegalStateException("상태값 설정 오류"));

        Waiting waiting = Waiting.builder()
                .operation(op)
                .member(member)
                .waitingNumber(nextNum)
                .teamSize(size)
                .waitingStatus(initStatus)
                .regDate(LocalDateTime.now())
                .build();

        return waitingRepository.save(waiting);
    }

    public boolean toggleWaitingOpen(Long storeId) {
        LocalDate today = LocalDate.now();
        WaitingOperation op = operationRepository.findByStoreIdAndOperationDate(storeId, today)
                .orElseGet(() -> {
                    WaitingStore store = storeRepository.findById(storeId).orElseThrow();
                    return operationRepository.save(WaitingOperation.builder()
                            .store(store).operationDate(today).status("CLOSED").lastWaitingNum(0).build());
                });

        boolean isOpen = "OPEN".equals(op.getStatus());
        String nextStatus = isOpen ? "CLOSED" : "OPEN";
        op.setStatus(nextStatus);

        saveLog(storeId, null, "TOGGLE", "영업 상태를 " + nextStatus + "로 변경");
        
        return !isOpen;
    }

    @Transactional(readOnly = true)
    public WaitingStore getStoreInfo(Long storeId) {
        WaitingStore store = storeRepository.findById(storeId).orElseThrow();
        boolean isOpen = operationRepository.findByStoreIdAndOperationDate(storeId, LocalDate.now())
                .map(op -> "OPEN".equals(op.getStatus()))
                .orElse(false);
        store.setWaitingOpen(isOpen);
        return store;
    }

    @Transactional(readOnly = true)
    public List<WaitingDTO> getStoreList(Long storeId) {
        return waitingRepository.findStoreList(storeId, LocalDate.now(), 
                List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()))
                .stream().map(WaitingDTO::new).collect(Collectors.toList());
    }

    // ================================================================================
    // 2. 상태 변경 액션
    // ================================================================================
    private void changeStatus(Long id, WaitingStatusType type) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow();
        WaitingStatus newStatus = statusRepository.findByStatusName(type.name()).orElseThrow();
        waiting.setWaitingStatus(newStatus);
        
        if (type == WaitingStatusType.CALLED) waiting.setCalledTime(LocalDateTime.now());
        if (type == WaitingStatusType.ENTERED) waiting.setSeatedTime(LocalDateTime.now());
    }

    public void call(Long id) {
        changeStatus(id, WaitingStatusType.CALLED);
        webSocketHandler.sendToCustomer(id, "CALL:입장해주세요! 🔔");
        
        Waiting w = waitingRepository.findById(id).orElseThrow();
        saveLog(w.getOperation().getStore().getId(), id, "CALL", w.getWaitingNumber() + "번 손님 호출");
    }

    public void enter(Long id) {
        changeStatus(id, WaitingStatusType.ENTERED);
        webSocketHandler.sendToCustomer(id, "ENTER:입장이 확인되었습니다.");
        
        Waiting w = waitingRepository.findById(id).orElseThrow();
        saveLog(w.getOperation().getStore().getId(), id, "ENTER", w.getWaitingNumber() + "번 손님 입장 완료");
    }

    public void cancel(Long id, boolean isStoreAction) {
        changeStatus(id, WaitingStatusType.CANCELED);
        if (isStoreAction) {
            webSocketHandler.sendToCustomer(id, "CANCEL:매장 사정으로 취소되었습니다. 😥");
            
            Waiting w = waitingRepository.findById(id).orElseThrow();
            saveLog(w.getOperation().getStore().getId(), id, "CANCEL", w.getWaitingNumber() + "번 손님 거절(취소)");
        }
    }

    public void postpone(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("정보 없음"));

        String currentStatus = waiting.getWaitingStatus().getStatusName();
        if ("ENTERED".equals(currentStatus) || "CANCELED".equals(currentStatus)) {
            throw new IllegalStateException("이미 종료된 웨이팅입니다.");
        }

        WaitingOperation op = waiting.getOperation();
        int nextNum = op.getLastWaitingNum() + 1;
        op.setLastWaitingNum(nextNum);
        
        waiting.setWaitingNumber(nextNum);

        if ("CALLED".equals(currentStatus)) {
            waiting.setWaitingStatus(statusRepository.findByStatusName("WAITING").get());
        }
    }
    
    public void notice(String content) {
        webSocketHandler.broadcast(content);
    }

    // ================================================================================
    // 3. 게시판형 공지사항 관리
    // ================================================================================
    @Transactional(readOnly = true)
    public List<WaitingNoticeDTO> getNoticeList(Long storeId) {
        return noticeRepository.findByStoreIdOrderByIsPinnedDescRegDateDesc(storeId)
                .stream().map(WaitingNoticeDTO::new).collect(Collectors.toList());
    }

    public void createNotice(Long storeId, String title, String content, boolean isPinned) {
        WaitingStore store = storeRepository.findById(storeId).orElseThrow();
        WaitingNotice notice = WaitingNotice.builder()
                .store(store)
                .title(title)
                .content(content)
                .isPinned(isPinned ? "Y" : "N")
                .build();
        noticeRepository.save(notice);
        saveLog(storeId, null, "NOTICE", "공지사항 등록: " + title);
    }

    public void updateNotice(Long noticeId, String title, String content, boolean isPinned) {
        WaitingNotice notice = noticeRepository.findById(noticeId).orElseThrow();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsPinned(isPinned ? "Y" : "N");
        saveLog(notice.getStore().getId(), null, "NOTICE", "공지사항 수정: " + title);
    }

    public void deleteNotice(Long noticeId) {
        WaitingNotice notice = noticeRepository.findById(noticeId).orElseThrow();
        Long storeId = notice.getStore().getId();
        noticeRepository.deleteById(noticeId);
        saveLog(storeId, null, "NOTICE", "공지사항 삭제 완료");
    }

    // ================================================================================
    // 4. 조회 및 기타
    // ================================================================================
    @Transactional(readOnly = true)
    public List<WaitingLog> getStoreLogs(Long storeId) {
        return logRepository.findTop50ByStoreIdOrderByRegDateDesc(storeId);
    }

    @Transactional(readOnly = true)
    public long getCountAhead(Long waitingId) {
        Waiting w = waitingRepository.findById(waitingId).orElseThrow();
        return waitingRepository.countAhead(w.getOperation().getId(), w.getWaitingNumber());
    }

    @Transactional(readOnly = true)
    public List<WaitingDTO> getMyHistory(Long userId) {
        return waitingRepository.findMemberHistory(userId, 
                List.of(WaitingStatusType.ENTERED.name(), WaitingStatusType.CANCELED.name()))
                .stream().map(WaitingDTO::new).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Waiting getMyActiveWaiting(Long userId) {
        List<Waiting> list = waitingRepository.findActiveWaiting(userId, 
            List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()));
        return list.isEmpty() ? null : list.get(0);
    }
    
    @Transactional(readOnly = true)
    public String getMyCurrentStatus(Long waitingId) {
        return waitingRepository.findById(waitingId)
                .map(w -> w.getWaitingStatus().getStatusName())
                .orElse("UNKNOWN");
    }
}