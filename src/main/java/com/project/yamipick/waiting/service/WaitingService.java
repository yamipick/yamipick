package com.project.yamipick.waiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.user.entity.User;  //  
import com.project.yamipick.user.repository.UserRepository;  //  
import com.project.yamipick.waiting.domain.StoreSchedule;
import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.domain.WaitingLog;
import com.project.yamipick.waiting.domain.WaitingNotice;
import com.project.yamipick.waiting.domain.WaitingOperation;
import com.project.yamipick.waiting.domain.WaitingStatus;
import com.project.yamipick.waiting.domain.WaitingStatusType;
import com.project.yamipick.waiting.domain.WaitingStore;
import com.project.yamipick.waiting.dto.StoreInfoDTO;
import com.project.yamipick.waiting.dto.StoreScheduleDTO;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.dto.WaitingNoticeDTO;
import com.project.yamipick.waiting.handler.WaitingWebSocketHandler;
import com.project.yamipick.waiting.repository.StoreScheduleRepository;
import com.project.yamipick.waiting.repository.WaitingLogRepository;
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
    private final UserRepository userRepository;  //  
    private final WaitingStoreRepository storeRepository;
    private final WaitingStatusRepository statusRepository;
    private final WaitingOperationRepository operationRepository;
    private final WaitingNoticeRepository noticeRepository;
    private final StoreScheduleRepository scheduleRepository;
    private final WaitingLogService logService;
    private final WaitingLogRepository logRepository;
    private final WaitingWebSocketHandler webSocketHandler;

    // ================================================================================
    // 🔒 내부 헬퍼: 로그 메시지 예쁘게 만들기
    // ================================================================================
    
    private String makeLogMsg(Waiting w, String action) {
        //  : w.getMember() → w.getUser()
        String storeName = w.getOperation().getStore().getName();
        String userName = w.getUser().getName();
        String phone = w.getUser().getPhone();
        return String.format("[%s] %d번 - %s(%s)님 %s",
                storeName, w.getWaitingNumber(), userName, phone, action);
    }

    // ================================================================================
    // 1. 웨이팅 등록
    // ================================================================================
    
    public Waiting register(Long userId, Long storeId, int size) {
        // 인원수 검증
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("인원수는 1명에서 100명 사이여야 합니다.");
        }

        //memberRepository → userRepository
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        // 중복 체크
        List<Waiting> activeWaitings = waitingRepository.findActiveWaiting(userId,
                List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()));
        
        if (!activeWaitings.isEmpty()) {
            throw new IllegalStateException("이미 진행 중인 웨이팅이 있습니다. (중복 접수 불가)");
        }

        // 오늘 날짜의 운영 정보 확인
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

        // 마감 체크
        if (!"OPEN".equals(op.getStatus())) {
            throw new IllegalStateException("⛔ 현재 웨이팅 접수가 마감되었습니다.");
        }

        // 번호표 발급
        int nextNum = op.getLastWaitingNum() + 1;
        op.setLastWaitingNum(nextNum);

        // 저장
        WaitingStatus initStatus = statusRepository.findByStatusName("WAITING")
                .orElseThrow(() -> new IllegalStateException("상태값 설정 오류"));
        
        //: .member(member) → .user(user)
        Waiting waiting = Waiting.builder()
                .operation(op)
                .user(user)  // ✅ 필드명 변경
                .waitingNumber(nextNum)
                .teamSize(size)
                .waitingStatus(initStatus)
                .regDate(LocalDateTime.now())
                .build();

        return waitingRepository.save(waiting);
    }

    // ================================================================================
    // 2. 영업 상태 토글
    // ================================================================================
    
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

        String storeName = op.getStore().getName();
        logService.saveLog(storeId, null, "TOGGLE", "[" + storeName + "] 영업 상태를 " + nextStatus + "로 변경");

        return !isOpen;
    }

    // ================================================================================
    // 3. 매장 정보 조회
    // ================================================================================
    
    @Transactional(readOnly = true)
    public StoreInfoDTO getStoreInfo(Long storeId) {
        WaitingStore store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장 정보 없음"));

        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue() % 7;

        boolean isOpen = operationRepository.findByStoreIdAndOperationDate(storeId, today)
                .map(op -> "OPEN".equals(op.getStatus()))
                .orElse(false);

        String hoursText = "영업 정보 없음";
        StoreSchedule schedule = scheduleRepository.findByStoreIdAndDayOfWeek(storeId, dayOfWeek).orElse(null);
        
        if (schedule != null) {
            if ("N".equals(schedule.getIsOpen())) {
                hoursText = "⛔ 오늘은 휴무입니다";
            } else {
                hoursText = schedule.getOpenTime() + " ~ " + schedule.getCloseTime();
                if (schedule.getBreakStart() != null && !schedule.getBreakStart().isEmpty()) {
                    hoursText += " (브레이크: " + schedule.getBreakStart() + "~" + schedule.getBreakEnd() + ")";
                }
            }
        }

        return new StoreInfoDTO(store, isOpen, hoursText);
    }

    @Transactional(readOnly = true)
    public List<WaitingDTO> getStoreList(Long storeId) {
        return waitingRepository.findStoreList(storeId, LocalDate.now(),
                List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()))
                .stream().map(WaitingDTO::new).collect(Collectors.toList());
    }

    // ================================================================================
    // 4. 상태 변경 액션
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
        logService.saveLog(w.getOperation().getStore().getId(), id, "CALL", makeLogMsg(w, "호출"));
    }

    public void enter(Long id) {
        changeStatus(id, WaitingStatusType.ENTERED);
        webSocketHandler.sendToCustomer(id, "ENTER:입장이 확인되었습니다.");
        
        Waiting w = waitingRepository.findById(id).orElseThrow();
        logService.saveLog(w.getOperation().getStore().getId(), id, "ENTER", makeLogMsg(w, "입장 완료"));
    }

    public void cancel(Long id, boolean isStoreAction) {
        changeStatus(id, WaitingStatusType.CANCELED);
        
        if (isStoreAction) {
            webSocketHandler.sendToCustomer(id, "CANCEL:매장 사정으로 취소되었습니다. 😥");
            Waiting w = waitingRepository.findById(id).orElseThrow();
            logService.saveLog(w.getOperation().getStore().getId(), id, "CANCEL", makeLogMsg(w, "거절(취소) 처리"));
        }
    }

    @Transactional
    public void postpone(Long waitingId, Long userId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 웨이팅입니다."));

        // ★ [핵심] 본인 확인 로직
        if (!waiting.getUser().getSeqUser().equals(userId)) {
            throw new IllegalStateException("본인의 웨이팅만 미룰 수 있습니다.");
        }
        
        String currentStatus = waiting.getWaitingStatus().getStatusName();
        if ("ENTERED".equals(currentStatus) || "CANCELED".equals(currentStatus)) {
            throw new IllegalStateException("이미 종료된 웨이팅입니다.");
        }

        WaitingOperation op = waiting.getOperation();
        if (waiting.getWaitingNumber() == op.getLastWaitingNum()) {
            throw new IllegalStateException("현재 가장 마지막 순서라 미룰 수 없습니다.");
        }

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
    
    
    @Transactional
    public void cancelByUser(Long waitingId, Long userId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 웨이팅입니다."));

        // ★ [핵심] 본인 확인 로직
        if (!waiting.getUser().getSeqUser().equals(userId)) {
            throw new IllegalStateException("본인의 웨이팅만 취소할 수 있습니다.");
        }

        // 검증 통과하면 기존 취소 로직 재사용 (또는 상태 변경 코드 직접 작성)
        // 여기서는 기존에 있던 cancel(id, isStoreAction)을 재활용한다고 가정
        cancel(waitingId, false); 
    }

    // ================================================================================
    // 5. 공지사항 관리
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
    // 6. 조회 메소드들
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

    @Transactional(readOnly = true)
    public Map<String, Object> getDailyReport(Long storeId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

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

    // ================================================================================
    // 7. 매장 검색
    // ================================================================================
    
    @Transactional(readOnly = true)
    public List<StoreInfoDTO> searchStores(String keyword) {
        List<WaitingStore> stores = storeRepository.findByNameContaining(keyword);
        if (stores.isEmpty()) return new java.util.ArrayList<>();

        List<Long> storeIds = stores.stream()
                .map(WaitingStore::getId)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        int dbDay = today.getDayOfWeek().getValue() % 7;

        Map<Long, WaitingOperation> opMap = operationRepository
                .findAllByStoreIdInAndOperationDate(storeIds, today).stream()
                .collect(Collectors.toMap(
                        op -> op.getStore().getId(),
                        op -> op));

        Map<Long, StoreSchedule> schMap = scheduleRepository
                .findAllByStoreIdInAndDayOfWeek(storeIds, dbDay).stream()
                .collect(Collectors.toMap(
                        sch -> sch.getStore().getId(),
                        sch -> sch));

        return stores.stream().map(store -> {
            Long storeId = store.getId();
            WaitingOperation op = opMap.get(storeId);
            boolean isOpenNow = op != null && "OPEN".equals(op.getStatus());

            StoreSchedule schedule = schMap.get(storeId);
            String hoursText = "정보 없음";
            if (schedule != null) {
                if ("N".equals(schedule.getIsOpen())) {
                    hoursText = "오늘은 휴무입니다";
                } else {
                    hoursText = schedule.getOpenTime() + " ~ " + schedule.getCloseTime();
                    if (schedule.getBreakStart() != null && !schedule.getBreakStart().isEmpty()) {
                        hoursText += " (브레이크타임 " + schedule.getBreakStart() + "~" + schedule.getBreakEnd() + ")";
                    }
                }
            }
            return new StoreInfoDTO(store, isOpenNow, hoursText);
        }).collect(Collectors.toList());
    }

    // ================================================================================
    // 8. 스케줄 관리
    // ================================================================================
    
    public void updateSchedule(StoreScheduleDTO dto) {
        WaitingStore store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장 없음"));

        for (Integer day : dto.getDays()) {
            StoreSchedule schedule = scheduleRepository.findByStoreIdAndDayOfWeek(dto.getStoreId(), day)
                    .orElseGet(() -> StoreSchedule.builder()
                            .store(store)
                            .dayOfWeek(day)
                            .build());

            schedule.setOpenTime(dto.getOpenTime());
            schedule.setCloseTime(dto.getCloseTime());
            schedule.setBreakStart(dto.getBreakStart());
            schedule.setBreakEnd(dto.getBreakEnd());
            schedule.setIsOpen(dto.getIsOpen());
            scheduleRepository.save(schedule);
        }
    }

    @Transactional(readOnly = true)
    public StoreScheduleDTO getScheduleInfo(Long storeId, int day) {
        return scheduleRepository.findByStoreIdAndDayOfWeek(storeId, day)
                .map(entity -> {
                    StoreScheduleDTO dto = new StoreScheduleDTO();
                    dto.setStoreId(entity.getStore().getId());
                    dto.setOpenTime(entity.getOpenTime());
                    dto.setCloseTime(entity.getCloseTime());
                    dto.setBreakStart(entity.getBreakStart());
                    dto.setBreakEnd(entity.getBreakEnd());
                    dto.setIsOpen(entity.getIsOpen());
                    return dto;
                })
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<StoreScheduleDTO> getAllSchedules(Long storeId) {
        return scheduleRepository.findAllByStoreId(storeId).stream()
                .map(s -> {
                    StoreScheduleDTO dto = new StoreScheduleDTO();
                    dto.setDayOfWeek(s.getDayOfWeek());
                    dto.setOpenTime(s.getOpenTime());
                    dto.setCloseTime(s.getCloseTime());
                    dto.setBreakStart(s.getBreakStart());
                    dto.setBreakEnd(s.getBreakEnd());
                    dto.setIsOpen(s.getIsOpen());
                    return dto;
                })
                .sorted(Comparator.comparingInt(StoreScheduleDTO::getDayOfWeek))
                .collect(Collectors.toList());
    }
}