package com.project.yamipick.store.service;

import org.springframework.stereotype.Service;

import com.project.yamipick.store.dto.StoreDTO;
import com.project.yamipick.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewStoreServiceImpl implements ReviewStoreService {

    private final StoreRepository storeRepository;

    @Override
    public StoreDTO getStore(Long seqStore) {
        return storeRepository.findById(seqStore)
                .map(entity -> StoreDTO.builder()
                        .seqStore(entity.getSeqStore())
                        .kakaoPlaceId(entity.getKakaoPlaceId())
                        .name(entity.getName())
                        .address(entity.getAddress())
                        .phone(entity.getPhone())
                        .build())
                .orElse(null);
    }
}
