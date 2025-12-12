package com.project.yamipick.review.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.user.entity.User;

public interface BoardReviewRepository extends JpaRepository<BoardReview, Long> {

    // 추천 게시글 (좋아요)
    @Query("""
        select r from BoardReview r
        where r.state = 'ACTIVE'
        order by r.favoriteCount desc, r.regdate desc
    """)
    List<BoardReview> findRecommendReviews(Pageable pageable);

    // 사진 리뷰
    @Query("""
        select r from BoardReview r
        where r.attach is not null
          and r.state = 'ACTIVE'
        order by r.regdate desc
    """)
    List<BoardReview> findPhotoReviews(Pageable pageable);

    // 일간 / 주간 인기
    @Query("""
        select r from BoardReview r
        where r.regdate >= :from
          and r.state = 'ACTIVE'
        order by r.favoriteCount desc, r.readCount desc
    """)
    List<BoardReview> findPopular(@Param("from") LocalDateTime from, Pageable pageable);

    // 최신 리뷰
    @Query("""
        select r from BoardReview r
        where r.state = 'ACTIVE'
        order by r.regdate desc
    """)
    List<BoardReview> findLatestReviews(Pageable pageable);

    // 내가 쓴 리뷰 수
    long countByUserAndState(User user, String state);

	List<BoardReview> findByState(String string, Pageable pageable);

	@Query(
		    value = """
		    select br.*
				from tblBoardReview br
				where br.state = 'ACTIVE'
				  and (
				        br.title like '%' || :keyword || '%'
				     or DBMS_LOB.SUBSTR(br.reviewContent, 4000, 1) like '%' || :keyword || '%'
				     or exists (
				         select 1
				         from tblTagging t
				         join tblHashtag h on h.seqHashtag = t.seqHashtag
				         where t.seqReview = br.seqReview
				           and h.hashtag like '%' || :keyword || '%'
				     )
				  )
				order by br.regdate desc
		    """,
		    nativeQuery = true
		)
		List<BoardReview> search(@Param("keyword") String keyword, Pageable pageable);

	
	
}
