package com.project.yamipick.waiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.waiting.domain.Member;
import com.project.yamipick.waiting.domain.Store;
import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.domain.WaitingOperation;
import com.project.yamipick.waiting.domain.WaitingStatus;
import com.project.yamipick.waiting.domain.WaitingStatusType;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.handler.WaitingWebSocketHandler;
import com.project.yamipick.waiting.repository.MemberRepository;
import com.project.yamipick.waiting.repository.StoreRepository;
import com.project.yamipick.waiting.repository.WaitingOperationRepository;
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
    private final WaitingOperationRepository operationRepository; // ★ 새로 추가된 리포지토리
    private final WaitingWebSocketHandler webSocketHandler;

    // ★ [삭제됨] 기존 메모리 Map (storeStatusMap)은 이제 사용하지 않습니다.

    /**
     * 1. 웨이팅 등록
     * - DB(WaitingOperation)에서 오늘 날짜의 매장 정보를 확인
     * - 정보가 없으면 자동 생성 (첫 손님인 경우)
     * - 영업 상태 확인 후 번호표(+1) 발급
     */
    public Waiting register(Long userId, int size) {
        Long storeId = 1L; // (테스트용 고정, 추후 파라미터로 받으세요)

        // 1. "오늘" 날짜의 매장 운영 정보 가져오기 (없으면 생성 로직 실행)
        LocalDate today = LocalDate.now();
        WaitingOperation op = operationRepository.findByStoreIdAndOperationDate(storeId, today)
                .orElseGet(() -> {
                    // 오늘 데이터가 없으면 새로 만듭니다. (자동 오픈 처리)
                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));
                    
                    WaitingOperation newOp = WaitingOperation.builder()
                            .store(store)
                            .operationDate(today)
                            .status("OPEN")       // 기본값: 영업중
                            .lastWaitingNum(0)    // 번호표 0번부터 시작
                            .build();
                    return operationRepository.save(newOp);
                });

        // 2. 영업 상태 체크 (DB 값 확인)
        if (!"OPEN".equals(op.getStatus())) {
            throw new IllegalStateException("⛔ 현재 웨이팅 접수가 마감되었습니다.");
        }

        // 3. 번호표 발급 (마지막 번호 + 1)
        int nextNum = op.getLastWaitingNum() + 1;
        op.setLastWaitingNum(nextNum); // 운영 정보 업데이트 (Dirty Checking)

        // 4. 웨이팅 저장
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        WaitingStatus initStatus = getStatusEntity(WaitingStatusType.WAITING);

        Waiting waiting = Waiting.builder()
                .operation(op)          // 운영 정보 연결
                .member(member)
                .waitingNumber(nextNum) // ★ 고정 대기번호 저장
                .teamSize(size)
                .waitingStatus(initStatus)
                .regDate(LocalDateTime.now())
                .build();
        
        return waitingRepository.save(waiting);
    }

    /**
     * 2. 매장 접수 상태 토글 (OPEN <-> CLOSED)
     * - 메모리가 아니라 DB(WaitingOperation)의 status 컬럼을 변경합니다.
     */
    public boolean toggleWaitingOpen(Long storeId) {
        LocalDate today = LocalDate.now();
        
        // 데이터가 없으면 생성 (register와 동일 로직)
        WaitingOperation op = operationRepository.findByStoreIdAndOperationDate(storeId, today)
                .orElseGet(() -> {
                    Store store = storeRepository.findById(storeId).orElseThrow();
                    return operationRepository.save(WaitingOperation.builder()
                            .store(store)
                            .operationDate(today)
                            .status("CLOSED") // 토글을 누른다는 건 제어를 하겠다는 뜻이므로 일단 닫힌 상태로 생성 후 반전
                            .lastWaitingNum(0)
                            .build());
                });

        // 상태 반전 로직
        boolean isOpen = "OPEN".equals(op.getStatus());
        String nextStatus = isOpen ? "CLOSED" : "OPEN";
        
        op.setStatus(nextStatus); // DB 업데이트
        
        return !isOpen; // 결과 리턴 (true: 영업중, false: 마감)
    }

    /**
     * 3. 매장 정보 조회
     * - 매장 기본 정보 + 오늘의 영업 상태(DB)를 합쳐서 반환
     */
    @Transactional(readOnly = true)
    public Store getStoreInfo(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장 정보 없음"));
        
        // 오늘의 상태 조회
        boolean isOpen = operationRepository.findByStoreIdAndOperationDate(storeId, LocalDate.now())
                .map(op -> "OPEN".equals(op.getStatus()))
                .orElse(false); // 데이터가 없으면 마감으로 간주
        
        store.setWaitingOpen(isOpen); // Transient 필드에 주입
        return store;
    }

    /**
     * 4. 매장용 대기 목록 조회
     * - 오늘 날짜의 데이터만 가져옵니다.
     */
    @Transactional(readOnly = true)
    public List<WaitingDTO> getStoreList(Long storeId) {
        return waitingRepository.findStoreList(storeId, LocalDate.now(), 
                List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()))
                .stream()
                .map(WaitingDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 5. 내 앞 대기 팀 수 확인
     * - WaitingOperation ID와 내 번호를 기준으로 계산합니다.
     */
    @Transactional(readOnly = true)
    public long getCountAhead(Long waitingId) {
        Waiting myWaiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("웨이팅 정보 없음"));
        
        Long opId = myWaiting.getOperation().getId();
        int myNum = myWaiting.getWaitingNumber();
        
        // 같은 매장, 같은 날짜(opId)에서 나보다 번호가 작은 사람 수
        return waitingRepository.countAhead(opId, myNum);
    }

    // --- 아래는 기존 로직과 동일 (상태 변경 및 웹소켓 알림) ---

    @Transactional(readOnly = true)
    public List<WaitingDTO> getMyHistory(Long userId) {
        List<Waiting> list = waitingRepository.findMemberHistory(userId, 
                List.of(WaitingStatusType.ENTERED.name(), WaitingStatusType.CANCELED.name()));
        return list.stream().map(WaitingDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getMyCurrentStatus(Long waitingId) {
        return waitingRepository.findById(waitingId)
                .map(w -> w.getWaitingStatus().getStatusName())
                .orElse("UNKNOWN");
    }
    
    @Transactional(readOnly = true)
    public Waiting getMyActiveWaiting(Long userId) {
        List<Waiting> list = waitingRepository.findActiveWaiting(userId, 
            List.of(WaitingStatusType.WAITING.name(), WaitingStatusType.CALLED.name()));
        return list.isEmpty() ? null : list.get(0);
    }

    // 상태 변경 공통 메서드
    private void changeStatus(Long id, WaitingStatusType type) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("웨이팅 정보 없음"));
        
        WaitingStatus newStatus = getStatusEntity(type);
        waiting.setWaitingStatus(newStatus);

        if (type == WaitingStatusType.CALLED) waiting.setCalledTime(LocalDateTime.now());
        if (type == WaitingStatusType.ENTERED) waiting.setSeatedTime(LocalDateTime.now());
    }

    private WaitingStatus getStatusEntity(WaitingStatusType type) {
        return statusRepository.findByStatusName(type.name())
                .orElseThrow(() -> new IllegalStateException("DB에 상태 데이터가 없습니다: " + type.name()));
    }

    // 호출 (웹소켓 알림 포함)
    public void call(Long id) {
        changeStatus(id, WaitingStatusType.CALLED);
        webSocketHandler.sendToCustomer(id, "CALL:입장해주세요! 🔔");
    }

    // 입장 완료
    public void enter(Long id) {
        changeStatus(id, WaitingStatusType.ENTERED);
        webSocketHandler.sendToCustomer(id, "ENTER:입장이 확인되었습니다.");
    }

    // 취소 (거절)
    public void cancel(Long id, boolean isStoreAction) {
        changeStatus(id, WaitingStatusType.CANCELED);
        if (isStoreAction) {
            webSocketHandler.sendToCustomer(id, "CANCEL:매장 사정으로 취소되었습니다. 😥");
        }
    }

    // 전체 공지
    public void notice(String content) {
        webSocketHandler.broadcast(content);
    }
}