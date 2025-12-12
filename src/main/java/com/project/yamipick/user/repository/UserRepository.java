package com.project.yamipick.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.project.yamipick.user.entity.User;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    long countByCreatedAtAfter(LocalDateTime date);

    long countByStatusUser(String statusUser);

    @Modifying
    @Query("UPDATE User u SET u.statusUser = 'ACTIVE', u.suspendedUntil = null " +
           "WHERE u.statusUser = 'SUSPENDED' AND u.suspendedUntil < :now")
    int updateStatusToActiveIfSuspensionEnded(@Param("now") LocalDateTime now);

    // ✅ [추가] 웨이팅 시스템에서 동시성 제어용 (비관적 락)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.seqUser = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);
}


