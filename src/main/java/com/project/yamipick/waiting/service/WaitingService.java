package com.project.yamipick.waiting.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.waiting.domain.Member;
import com.project.yamipick.waiting.domain.Store;
import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.domain.WaitingStatus;
import com.project.yamipick.waiting.domain.WaitingStatusType;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.handler.WaitingWebSocketHandler;
import com.project.yamipick.waiting.repository.MemberRepository;
import com.project.yamipick.waiting.repository.StoreRepository;
import com.project.yamipick.waiting.repository.WaitingRepository;
import com.project.yamipick.waiting.repository.WaitingStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final WaitingStatusRepository statusRepository;
    private final WaitingWebSocketHandler webSocketHandler;

    // ★ [핵심] DB 컬럼 대신 메모리에서 영업 상태 관리 (Key: storeId, Value: isOpen)
    // 서버 재시작 전까지 상태가 유지됩니다.
    private static final Map<Long, Boolean> storeStatusMap = new ConcurrentHashMap<>();

    // 1. 웨이팅 등록
    public Waiting register(Long userId, int size) {
        Store store = storeRepository.findById(1L).orElseThrow();

        // ★ [수정] DB 값이 아니라, 메모리 맵의 상태를 확인
        // 맵에 값이 없으면 기본값 true(영업중)으로 간주
        boolean isOpen = storeStatusMap.getOrDefault(store.getId(), true);
        
        if (!isOpen) {
            throw new IllegalStateException("⛔ 현재 웨이팅 접수가 마감되었습니다.");
        }

        Member member = memberRepository.findById(userId).orElseThrow();
        WaitingStatus initStatus = getStatusEntity(WaitingStatusType.WAITING);

        Waiting waiting = Waiting.builder()
                .member(member).store(store).teamSize(size)
                .waitingStatus(initStatus)
                .regDate(LocalDateTime.now())
                .build();
        return waitingRepository.save(waiting);
    }

    // 2. 매장 접수 상태 토글 (메모리 맵 업데이트)
    public boolean toggleWaitingOpen(Long storeId) {
        // 현재 상태 가져오기 (없으면 true)
        boolean currentStatus = storeStatusMap.getOrDefault(storeId, true);
        
        // 상태 반전
        boolean nextStatus = !currentStatus;
        
        // 맵에 저장 (이제 이 값은 서버 끄기 전까지 유지됨)
        storeStatusMap.put(storeId, nextStatus);
        
        return nextStatus;
    }

    // 3. 매장 정보 조회 (상태 덮어쓰기)
    @Transactional(readOnly = true)
    public Store getStoreInfo(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        
        // ★ DB에서 가져온 객체에 메모리에 있는 실제 상태를 주입
        boolean realStatus = storeStatusMap.getOrDefault(storeId, true);
        store.setWaitingOpen(realStatus);
        
        return store;
    }

    // ... (아래는 기존 로직과 동일하지만, DTO 변환 등 유지) ...

    private void changeStatus(Long id, WaitingStatusType type) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow();
        WaitingStatus newStatus = getStatusEntity(type);
        waiting.setWaitingStatus(newStatus);

        if (type == WaitingStatusType.CALLED) waiting.setCalledTime(LocalDateTime.now());
        if (type == WaitingStatusType.ENTERED) waiting.setSeatedTime(LocalDateTime.now());
    }

    private WaitingStatus getStatusEntity(WaitingStatusType type) {
        return statusRepository.findByStatusName(type.name())
                .orElseThrow(() -> new IllegalStateException("상태 데이터 없음: " + type.name()));
    }

    public void call(Long id) {
        changeStatus(id, WaitingStatusType.CALLED);
        webSocketHandler.sendToCustomer(id, "CALL:입장해주세요! 🔔");
    }

    public void enter(Long id) {
        changeStatus(id, WaitingStatusType.ENTERED);
        webSocketHandler.sendToCustomer(id, "ENTER:입장이 확인되었습니다.");
    }

    public void cancel(Long id, boolean isStoreAction) {
        changeStatus(id, WaitingStatusType.CANCELED);
        // ★ 매장 취소 시 알림 발송
        if (isStoreAction) {
            webSocketHandler.sendToCustomer(id, "CANCEL:매장 사정으로 취소되었습니다. 😥");
        }
    }

    public void notice(String content) {
        webSocketHandler.broadcast(content);
    }

    @Transactional(readOnly = true)
    public List<WaitingDTO> getStoreList(Long storeId) {
        return waitingRepository.findStoreList(storeId, List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()))
                .stream().map(WaitingDTO::new).collect(Collectors.toList());
    }

 // ★ [수정] 기록 조회: Entity -> DTO 변환 (이제 기록이 잘 뜰 겁니다!)
    @Transactional(readOnly = true)
    public List<WaitingDTO> getMyHistory(Long userId) {
        List<Waiting> list = waitingRepository.findMemberHistory(userId, 
                List.of(WaitingStatusType.ENTERED.name(), WaitingStatusType.CANCELED.name()));
        
        return list.stream().map(WaitingDTO::new).collect(Collectors.toList());
    }

    // ★ [추가] 내 현재 상태 조회 (웹소켓 실패 대비용)
    @Transactional(readOnly = true)
    public String getMyCurrentStatus(Long waitingId) {
        return waitingRepository.findById(waitingId)
                .map(w -> w.getWaitingStatus().getStatusName())
                .orElse("UNKNOWN");
    }

    @Transactional(readOnly = true)
    public long getCountAhead(Long id) {
        return waitingRepository.countAhead(id);
    }
    
    @Transactional(readOnly = true)
    public Waiting getMyActiveWaiting(Long userId) {
        List<Waiting> list = waitingRepository.findMemberHistory(userId, 
            List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()));
        return list.isEmpty() ? null : list.get(0);
    }
}