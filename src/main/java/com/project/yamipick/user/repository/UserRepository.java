package com.project.yamipick.user.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserId(String userId); 
    
    long countByCreatedAt(LocalDate date);
}
