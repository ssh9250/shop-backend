package com.study.shop.domain.comment.repository;

import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    @Query("select c from Comment c where c.member.id = :memberId")
    List<Comment> findActiveCommentByMemberId(Long memberId);

    // 관리자용: 소프트 삭제된 댓글 포함 조회
    @Query("select c from Comment c where c.post.id = :postId")
    List<Comment> findAllByPostId(Long postId);

    @Modifying(clearAutomatically = true)
    @Query("update Comment c set c.deletedAt = NOW() where c.post.id = :postId ")
    void deleteAllByPostId(Long postId);

    @Modifying(clearAutomatically = true)
    @Query("update Comment c set c.deletedAt = NOW() where c.member.id = :memberId")
    void softDeleteAllByMember(Long memberId);
}
