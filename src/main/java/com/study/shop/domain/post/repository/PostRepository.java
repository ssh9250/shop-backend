package com.study.shop.domain.post.repository;

import com.study.shop.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>,PostRepositoryCustom {
    @Query("select p from Post p where p.member.id = :memberId")
    List<Post> findByMemberId(Long memberId);

    @Query(value = "select p from Post p join fetch p.member",
            countQuery = "select count(p) from Post p")
    Page<Post> findAllWithMember(Pageable pageable);

    // jpa에서는 기본적으로 1:n 중복 열에 대한 처리를 하긴 하지만, 그래도 distinct 작성해주자
    @Query("select distinct p " +
            "from Post p " +
            "join fetch p.member m " +
            "left join fetch p.comments c " +
//            "left join fetch c.member " +
            // comment에 writer 필드가 있으므로 불필요
            "where p.id = :postId")
    Optional<Post> findPostByIdWithComment(Long postId);
}

// 회원목록 -> 한 회원 조회 -> 회원이 쓴 글 목록 -> 글 클릭 시 포스트로 이동
// find all,

// json 직렬화 n+1

// queryDSL dto projection

// member 조회할때 post fetch join