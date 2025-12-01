package com.project.yamipick.waiting.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.yamipick.waiting.domain.Member;
import com.project.yamipick.waiting.domain.Store;
import com.project.yamipick.waiting.repository.MemberRepository;
import com.project.yamipick.waiting.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DummyDataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    @Override
    public void run(String... args) throws Exception {
        // 서버 켜질 때 데이터 없으면 생성
        if (memberRepository.count() == 0) {
            memberRepository.save(Member.builder().name("테스터").phoneNumber("010-1234-5678").build());
        }
        if (storeRepository.count() == 0) {
            storeRepository.save(Store.builder().name("야미식당").build());
        }
    }
}
