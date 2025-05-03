package com.study.cruisin.domain.comment.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.cruisin.domain.comment.entity.Comment;
import com.study.cruisin.domain.comment.entity.QComment;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.study.cruisin.domain.comment.entity.QComment.*;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{
    // querydsl
    private final JPAQueryFactory queryFactory;
    @Override
    public List<Comment> findByPostId(Long postId) {
        return queryFactory
                .selectFrom(comment)
                .where(getEq(postId))
                .orderBy(comment.createdAt.desc())
                .fetch();
    }

    private static BooleanExpression getEq(Long id) {
        if (id == null) {
            return null;
        }
        return comment.post.id.eq(id);
    }
}
