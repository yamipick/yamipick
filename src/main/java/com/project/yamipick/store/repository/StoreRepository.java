package com.project.yamipick.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.store.entity.Store;
import com.project.yamipick.user.entity.User;

public interface StoreRepository extends JpaRepository<Store, Long>{

	// 이 유저에게 매장이 있는지
    Optional<Store> findByUser(User user);

    boolean existsByUser(User user);

    // kakaoPlaceId 중복 체크
    boolean existsByKakaoPlaceId(String kakaoPlaceId);

	List<Store> findByNameContainingIgnoreCase(String keyword);
	
}
