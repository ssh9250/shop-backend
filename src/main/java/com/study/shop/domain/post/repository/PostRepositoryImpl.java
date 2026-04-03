package com.study.shop.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.shop.domain.post.dto.PostListDto;
import com.study.shop.domain.post.dto.PostSearchConditionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.study.shop.domain.comment.entity.QComment.comment;
import static com.study.shop.domain.member.entity.QMember.member;
import static com.study.shop.domain.post.entity.QPost.post;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostListDto> searchPosts(PostSearchConditionDto cond, Pageable pageable) {
        List<PostListDto> content = queryFactory
                .select(Projections.constructor(PostListDto.class,
                        post.id, post.title, member.nickname, post.createdAt, comment.count(), post.viewCount
                ))
                .from(post)
                .join(post.member, member)
                .leftJoin(post.comments, comment)
                .where(
                        titleContains(cond.getTitle()),
                        contentContains(cond.getContent()),
                        writerContains(cond.getWriter()),
//                        hiddenEq(cond.getHidden()) 관리자용
                        hiddenEq(false),
                        createdAtAfter(cond.getFrom()),
                        createdAtBefore(cond.getTo())
                )
                .groupBy(post.id)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(post.count())
                .from(post)
                .join(post.member, member)
                .where(
                        titleContains(cond.getTitle()),
                        contentContains(cond.getContent()),
                        writerContains(cond.getWriter()),
//                        hiddenEq(cond.getHidden()) 관리자용
                        hiddenEq(false),
                        createdAtAfter(cond.getFrom()),
                        createdAtBefore(cond.getTo())
                )
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression titleContains(String title) {
        return hasText(title) ? post.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return hasText(content) ? post.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression writerContains(String writer) {
        return hasText(writer) ? member.nickname.containsIgnoreCase(writer) : null;
    }

    private BooleanExpression hiddenEq(Boolean hidden) {
        return hidden != null ? post.hidden.eq(hidden) : null;
    }

    private BooleanExpression createdAtAfter(LocalDateTime from) {
        return from != null ? post.createdAt.goe(from) : null;
    }

    private BooleanExpression createdAtBefore(LocalDateTime to) {
        return to != null ? post.createdAt.loe(to) : null;
    }
}
