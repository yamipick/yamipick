package com.project.yamipick.store.repository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> d78f139d2ff0a8dd6bdd0c2732c8e20d7eac2cde
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

	Optional<User> findByKakaoPlaceId(String kakaoPlaceId);
	
}
