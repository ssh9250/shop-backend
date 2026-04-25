package com.study.shop.domain.member.repository;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.shop.domain.member.dto.MemberListResponseDto;
import com.study.shop.domain.member.dto.MemberSearchConditionDto;
import com.study.shop.domain.post.dto.PostSearchConditionDto;
import com.study.shop.global.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.study.shop.domain.member.entity.QMember.member;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Repository
public class MemberQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<MemberListResponseDto> searchMembers(MemberSearchConditionDto cond) {
        return queryFactory
                .select(Projections.constructor(MemberListResponseDto.class,
                        member.email, member.nickname, member.role))
                .from(member)
                .where(emailContains(cond.getEmail()),
                        roleContains(cond.getRoleType()),
                        deletedEq(cond.getIsDeleted()))
                .fetch();
    }

    private BooleanExpression emailContains(String email) {
        return hasText(email) ? member.email.containsIgnoreCase(email) : null;
    }
    private BooleanExpression roleContains(RoleType role) {
        return role == null ? null : member.role.eq(role);
    }
    private BooleanExpression deletedEq(Boolean deleted) {
        if (deleted == null)
            return null;
        return deleted ? member.deletedAt.isNotNull() : member.deletedAt.isNull();
    }
}
