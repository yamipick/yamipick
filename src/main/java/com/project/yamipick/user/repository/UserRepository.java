package com.project.yamipick.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
