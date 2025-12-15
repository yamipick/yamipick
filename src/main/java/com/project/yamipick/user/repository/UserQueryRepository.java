package com.project.yamipick.user.repository;

import static com.project.yamipick.user.entity.QUser.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.project.yamipick.user.entity.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 회원 검색 + 페이징
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        
        // 1. 데이터 조회
        List<User> content = queryFactory
                .selectFrom(user)
                .where(
                    containsKeyword(keyword) // 검색 조건 (아이디 or 이름 or 닉네임)
                )
                .orderBy(user.seqUser.desc()) // 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회 (페이징 버튼 계산용)
        Long count = queryFactory
                .select(user.count())
                .from(user)
                .where(containsKeyword(keyword))
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }

    // [검색 조건] 키워드가 있으면 아이디/이름/닉네임 중에 하나라도 포함되면 OK
    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return user.userId.contains(keyword)
                .or(user.name.contains(keyword))
                .or(user.nickname.contains(keyword));
    }
}