package com.study.shop.domain.comment.repository;

import com.study.shop.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    @Query("select c from Comment c where c.member.id = :memberId and c.deleted = false")
    List<Comment> findActiveCommentByMemberId(Long memberId);

    // 관리자용: 소프트 삭제된 댓글 포함 조회
    @Query("select c from Comment c where c.post.id = :postId")
    List<Comment> findAllByPostId(Long postId);
}
