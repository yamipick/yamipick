package com.project.yamipick.waiting.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.waiting.domain.Member;
import com.project.yamipick.waiting.domain.Store;
import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.domain.WaitingStatus;
import com.project.yamipick.waiting.handler.WaitingWebSocketHandler;
import com.project.yamipick.waiting.repository.MemberRepository;
import com.project.yamipick.waiting.repository.StoreRepository;
import com.project.yamipick.waiting.repository.WaitingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final WaitingWebSocketHandler webSocketHandler;

    // 등록
    public Waiting register(Long userId, int size) {
        Member member = memberRepository.findById(userId).orElseThrow();
        Store store = storeRepository.findById(1L).orElseThrow(); // 더미 1번 매장

        Waiting waiting = Waiting.builder()
                .member(member).store(store).teamSize(size)
                .status(WaitingStatus.WAITING).regDate(LocalDateTime.now())
                .build();
        return waitingRepository.save(waiting);
    }

    // 호출
    public void call(Long id) {
        updateStatus(id, WaitingStatus.CALLED);
        webSocketHandler.sendToCustomer(id, "CALL:입장해주세요! 🔔");
    }

    // 입장
    public void enter(Long id) {
        updateStatus(id, WaitingStatus.ENTERED);
        webSocketHandler.sendToCustomer(id, "ENTER:입장이 확인되었습니다.");
    }

    // 취소/거절
    public void cancel(Long id) {
        updateStatus(id, WaitingStatus.CANCELED);
    }

    // 공지
    public void notice(String content) {
        webSocketHandler.broadcast(content);
    }

    private void updateStatus(Long id, WaitingStatus status) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow();
        waiting.setStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Waiting> getStoreList(Long storeId) {
        return waitingRepository.findByStoreIdAndStatusInOrderByRegDateAsc(
                storeId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));
    }

    @Transactional(readOnly = true)
    public long getCountAhead(Long id) {
        return waitingRepository.countByStatusAndIdLessThan(WaitingStatus.WAITING, id);
    }
}