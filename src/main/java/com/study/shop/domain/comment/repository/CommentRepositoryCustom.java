package com.study.shop.domain.comment.repository;

import com.study.shop.domain.comment.entity.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findByPostId(Long postId);

}
