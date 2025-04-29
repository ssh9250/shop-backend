package com.study.cruisin.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.cruisin.domain.comment.entity.Comment;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.study.cruisin.domain.comment.entity.QComment.comment;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    @Override
    public List<Comment> findByPostId(Long postId) {
        return queryFactory
                .selectFrom(comment)
                .where(comment.post.id.eq(postId))
                .orderBy(comment.createdAt.desc())
                .fetch();
    }
}
