package com.project.yamipick.waiting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.yamipick.waiting.domain.WaitingMember;

import jakarta.persistence.LockModeType;

public interface WaitingMemberRepository extends JpaRepository<WaitingMember, Long> {
	
	
	// ★ [추가] 유저를 조회할 때 락(Lock)을 걸어버림 (동시 진입 차단)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM WaitingMember m WHERE m.id = :id")
    Optional<WaitingMember> findByIdWithLock(@Param("id") Long id);
    
    //로그인
    Optional<WaitingMember> findByLoginId(String loginId);

}
