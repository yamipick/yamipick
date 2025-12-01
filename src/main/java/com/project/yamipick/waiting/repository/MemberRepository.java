package com.project.yamipick.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.waiting.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
