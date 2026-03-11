package com.study.shop.domain.post.repository;

import com.study.shop.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>,PostRepositoryCustom {
    @Query("select p from Post p where p.member.id = :memberId")
    List<Post> findByMemberId(Long memberId);

    @Query(value = "select p from Post p join fetch p.member",
            countQuery = "select count(p) from Post p")
    Page<Post> findAllWithMember(Pageable pageable);
}

// 회원목록 -> 한 회원 조회 -> 회원이 쓴 글 목록 -> 글 클릭 시 포스트로 이동
// find all,

// json 직렬화 n+1

// queryDSL dto projection

// member 조회할때 post fetch join