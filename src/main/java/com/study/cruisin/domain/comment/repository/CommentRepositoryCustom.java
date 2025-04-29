package com.study.cruisin.domain.comment.repository;

import com.study.cruisin.domain.comment.entity.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findByPostId(Long postId);

}
