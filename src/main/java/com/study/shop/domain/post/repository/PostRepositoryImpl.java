package com.study.shop.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.shop.domain.post.dto.PostListDto;
import com.study.shop.domain.post.dto.PostSearchConditionDto;
import com.study.shop.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.study.shop.domain.comment.entity.QComment.comment;
import static com.study.shop.domain.member.entity.QMember.member;
import static com.study.shop.domain.post.entity.QPost.post;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostListDto> findAllPosts(Pageable pageable) {
        // DTO Projection
        List<PostListDto> content = queryFactory
                .select(Projections.constructor(PostListDto.class,
                        post.id, post.title, post.content, post.writer, post.createdAt, comment.count()
                ))
                .from(post)
                .join(post.member, member)
                .leftJoin(post.comments, comment)
                .groupBy(post.id)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
//                .join(post.member, member) 현재는 생략 가능 (member가 반드시 있다는 보장 하에, 애초에 보장이 없으면 위에도 left join 해야되서 복잡해짐)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Post> searchPosts(PostSearchConditionDto cond) {
        return queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()
                .where(
                        titleContains(cond.getTitle()),
                        nicknameEq(cond.getNickname())
                )
                .orderBy(post.createdAt.desc())
                .fetch();
    }

    private BooleanExpression titleContains(String title) {
        return hasText(title) ? post.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression nicknameEq(String nickname) {
        return hasText(nickname) ? member.nickname.eq(nickname) : null;
    }
}
