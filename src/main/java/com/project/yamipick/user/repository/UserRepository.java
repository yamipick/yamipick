package com.project.yamipick.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.project.yamipick.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserId(String userId); 
    
    long countByCreatedAtAfter(LocalDateTime date);
    
    long countByStatusUser(String statusUser);

    @Modifying
    @Query("UPDATE User u SET u.statusUser = 'ACTIVE', u.suspendedUntil = null WHERE u.statusUser = 'SUSPENDED' AND u.suspendedUntil < :now")
    int updateStatusToActiveIfSuspensionEnded(@Param("now") LocalDateTime now);
}
