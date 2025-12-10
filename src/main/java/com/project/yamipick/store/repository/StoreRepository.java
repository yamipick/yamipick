package com.project.yamipick.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.store.entity.Store;
import com.project.yamipick.user.entity.User;

public interface StoreRepository extends JpaRepository<Store, Long>{

	Optional<User> findByKakaoPlaceId(String kakaoPlaceId);

	
}
