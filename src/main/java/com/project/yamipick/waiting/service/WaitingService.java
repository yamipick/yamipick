package com.project.yamipick.waiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    
    // ★ [변경] 로그 레포지토리 직접 사용 X -> 트랜잭션 분리된 서비스 사용
    private final WaitingLogService logService; 
    
    // ★ [추가] 리포트용 조회 때문에 필요하면 남겨둠 (단순 조회는 서비스 안 거쳐도 되지만 통계용)
    private final WaitingLogRepository logRepository;

    private final WaitingWebSocketHandler webSocketHandler;

    // ================================================================================
    // 🔒 내부 헬퍼: 로그 메시지 예쁘게 만들기
    // ================================================================================
    private String makeLogMsg(Waiting w, String action) {
        // 예: "[야미식당] 5번 - 홍길동(010-1234-5678)님 입장 완료"
        String storeName = w.getOperation().getStore().getName();
        String memberName = w.getMember().getName();
        String phone = w.getMember().getPhoneNumber();
        
        return String.format("[%s] %d번 - %s(%s)님 %s", 
                storeName, w.getWaitingNumber(), memberName, phone, action);
    }

    // ================================================================================
    // 1. 웨이팅 등록 및 운영 (손님/매장 공통)
    // ================================================================================

    /**
     * 웨이팅 등록
     */
public Waiting register(Long userId, Long storeId, int size) {
        
		
		
		// ★ [추가] 인원수 검증 (1명~100명까지만 허용)
    	if (size < 1 || size > 100) {
        throw new IllegalArgumentException("인원수는 1명에서 100명 사이여야 합니다.");
    }
        // 1. [순서 변경 & 락 적용] 유저 정보부터 가져오면서 락을 겁니다!
        // 이 순간, 동일한 userId로 들어온 다른 요청은 여기서 "일시 정지" 됩니다.
        WaitingMember member = memberRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        // 2. [중복 체크] 락이 걸린 상태에서 안전하게 중복 검사
        List<Waiting> activeWaitings = waitingRepository.findActiveWaiting(userId, 
                List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()));
        
        if (!activeWaitings.isEmpty()) {
            throw new IllegalStateException("이미 진행 중인 웨이팅이 있습니다. (중복 접수 불가)");
        }

        // 3. 오늘 날짜의 운영 정보 확인 (매장 락은 그대로 유지)
        LocalDate today = LocalDate.now();
        WaitingOperation op = operationRepository.findByStoreIdAndOperationDate(storeId, today)
                .orElseGet(() -> {
                     // ... (기존 로직 동일)
                     WaitingStore store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new IllegalArgumentException("매장 정보 없음"));
                    
                    return operationRepository.save(WaitingOperation.builder()
                            .store(store)
                            .operationDate(today)
                            .status("OPEN")
                            .lastWaitingNum(0)
                            .build());
                });

        // 4. 마감 체크
        if (!"OPEN".equals(op.getStatus())) {
            throw new IllegalStateException("⛔ 현재 웨이팅 접수가 마감되었습니다.");
        }

        // 5. 번호표 발급 (+1)
        int nextNum = op.getLastWaitingNum() + 1;
        op.setLastWaitingNum(nextNum); 

        // 6. 저장
        WaitingStatus initStatus = statusRepository.findByStatusName("WAITING")
                .orElseThrow(() -> new IllegalStateException("상태값 설정 오류"));

        Waiting waiting = Waiting.builder()
                .operation(op)
                .member(member) // 아까 위에서 조회한 member 사용
                .waitingNumber(nextNum)
                .teamSize(size)
                .waitingStatus(initStatus)
                .regDate(LocalDateTime.now())
                .build();

        return waitingRepository.save(waiting);
    }

    /**
     * 영업 상태 토글 (OPEN <-> CLOSED)
     */
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

        // ★ [수정] 로그 상세화 & 독립 트랜잭션 서비스 호출
        String storeName = op.getStore().getName();
        logService.saveLog(storeId, null, "TOGGLE", "[" + storeName + "] 영업 상태를 " + nextStatus + "로 변경");
        
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
    // 2. 상태 변경 액션 (호출, 입장, 취소, 미루기)
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
        
        // ★ [수정] 상세 로그 & 독립 저장
        Waiting w = waitingRepository.findById(id).orElseThrow();
        logService.saveLog(w.getOperation().getStore().getId(), id, "CALL", makeLogMsg(w, "호출"));
    }

    public void enter(Long id) {
        changeStatus(id, WaitingStatusType.ENTERED);
        webSocketHandler.sendToCustomer(id, "ENTER:입장이 확인되었습니다.");
        
        // ★ [수정] 상세 로그 & 독립 저장
        Waiting w = waitingRepository.findById(id).orElseThrow();
        logService.saveLog(w.getOperation().getStore().getId(), id, "ENTER", makeLogMsg(w, "입장 완료"));
    }

    public void cancel(Long id, boolean isStoreAction) {
        changeStatus(id, WaitingStatusType.CANCELED);
        if (isStoreAction) {
            webSocketHandler.sendToCustomer(id, "CANCEL:매장 사정으로 취소되었습니다. 😥");
            
            // ★ [수정] 상세 로그 & 독립 저장 (매장 취소 시에만)
            Waiting w = waitingRepository.findById(id).orElseThrow();
            logService.saveLog(w.getOperation().getStore().getId(), id, "CANCEL", makeLogMsg(w, "거절(취소) 처리"));
        }
    }

    /**
     * 순서 미루기 (맨 뒤로 이동)
     */
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
    // 3. 게시판형 공지사항 관리 (CRUD)
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
        
        // 공지는 실패해도 상관없으므로 굳이 트랜잭션 분리 안 해도 되지만, 통일성을 위해 사용 가능
        logService.saveLog(storeId, null, "NOTICE", "공지사항 등록: " + title);
        
        webSocketHandler.broadcast("REFRESH_NOTICE");
    }

    public void updateNotice(Long noticeId, String title, String content, boolean isPinned) {
        WaitingNotice notice = noticeRepository.findById(noticeId).orElseThrow();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsPinned(isPinned ? "Y" : "N");
        
        logService.saveLog(notice.getStore().getId(), null, "NOTICE", "공지사항 수정: " + title);
        
        webSocketHandler.broadcast("REFRESH_NOTICE");
    }

    public void deleteNotice(Long noticeId) {
        WaitingNotice notice = noticeRepository.findById(noticeId).orElseThrow();
        Long storeId = notice.getStore().getId();
        noticeRepository.deleteById(noticeId);
        
        logService.saveLog(storeId, null, "NOTICE", "공지사항 삭제 완료");
        
        webSocketHandler.broadcast("REFRESH_NOTICE");
    }

    // ================================================================================
    // 4. 조회 및 기타 (로그, 내 정보 등)
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

    // ★ [추가] 날짜별 로그 조회 서비스 (통계 페이지용)
    @Transactional(readOnly = true)
    public Map<String, Object> getDailyReport(Long storeId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 이 메서드는 WaitingLogRepository에 추가되어 있어야 함
        List<WaitingLog> logs = logRepository.findAllByStoreIdAndRegDateBetweenOrderByRegDateDesc(storeId, start, end);

        long totalCalls = logs.stream().filter(l -> "CALL".equals(l.getActionType())).count();
        long totalEnters = logs.stream().filter(l -> "ENTER".equals(l.getActionType())).count();
        long totalCancels = logs.stream().filter(l -> "CANCEL".equals(l.getActionType())).count();

        return Map.of(
            "logs", logs,
            "stats", Map.of(
                "calls", totalCalls,
                "enters", totalEnters,
                "cancels", totalCancels
            )
        );
    }
    
    
 // ★ [추가] 매장 검색 (영업 상태 포함)
    @Transactional(readOnly = true)
    public List<WaitingStore> searchStores(String keyword) {
        // 1. 이름으로 매장들 찾기
        List<WaitingStore> stores = storeRepository.findByNameContaining(keyword);
        
        // 2. 각 매장별로 오늘 영업 중인지 확인해서 세팅
        LocalDate today = LocalDate.now();
        for (WaitingStore store : stores) {
            // 운영 기록(Operation)이 있고, 상태가 'OPEN'이어야 영업 중
            boolean isOpen = operationRepository.findByStoreIdAndOperationDate(store.getId(), today)
                    .map(op -> "OPEN".equals(op.getStatus()))
                    .orElse(false); // 기록 없으면 영업 안 함(false)
            
            store.setWaitingOpen(isOpen);
        }
        
        return stores;
    }
}