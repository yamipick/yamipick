package com.project.yamipick.waiting.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.yamipick.waiting.domain.WaitingMember;
import com.project.yamipick.waiting.domain.WaitingStore;
import com.project.yamipick.waiting.domain.WaitingStatus;
import com.project.yamipick.waiting.domain.WaitingStatusType;
import com.project.yamipick.waiting.repository.WaitingMemberRepository;
import com.project.yamipick.waiting.repository.WaitingStoreRepository;
import com.project.yamipick.waiting.repository.WaitingStatusRepository;

import lombok.RequiredArgsConstructor;

//@Component
@RequiredArgsConstructor
public class DummyDataLoader implements CommandLineRunner {

    private final WaitingMemberRepository memberRepository;
    private final WaitingStoreRepository storeRepository;
    private final WaitingStatusRepository statusRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. [필수] 상태값 기초 데이터 생성 (없으면 에러남!)
        // Enum 이름 그대로 DB에 넣습니다. (WAITING, CALLED...)
        for (WaitingStatusType type : WaitingStatusType.values()) {
            createStatusIfAbsent(type.name());
        }

        // 2. 더미 회원 (DDL NOT NULL 컬럼 채움)
        if (memberRepository.count() == 0) {
            memberRepository.save(WaitingMember.builder()
                .name("테스터").phoneNumber("010-1234-5678")
                .loginId("test").password("1234").email("test@t.com")
                .nickname("T").statusUser("ACTIVE").role("USER").penaltyScore(0)
                .build());
        }

        // 3. 더미 매장
        if (storeRepository.count() == 0) {
            storeRepository.save(WaitingStore.builder()
                .name("야미식당")
                .waitingOpen(true) // 메모리용 (DB 저장 안됨)
                .kakaoPlaceId("k1").address("서울").ownerId(1L)
                .build());
        }
    }

    private void createStatusIfAbsent(String name) {
        if (statusRepository.findByStatusName(name).isEmpty()) {
            statusRepository.save(WaitingStatus.builder().statusName(name).build());
        }
    }
}