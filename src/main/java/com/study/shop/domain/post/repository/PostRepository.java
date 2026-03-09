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