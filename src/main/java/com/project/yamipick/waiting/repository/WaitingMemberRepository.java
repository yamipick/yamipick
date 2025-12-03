package com.project.yamipick.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.WaitingMember;

public interface WaitingMemberRepository extends JpaRepository<WaitingMember, Long> {

}
