package com.study.shop.domain.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.shop.domain.post.dto.PostSearchConditionDto;
import com.study.shop.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.study.shop.domain.member.entity.QMember.member;
import static com.study.shop.domain.post.entity.QPost.post;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostListDto>

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
