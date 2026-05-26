package com.study.shop.domain.member.repository;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.shop.domain.member.dto.MemberListResponseDto;
import com.study.shop.domain.member.dto.MemberSearchConditionDto;
import com.study.shop.global.enums.RoleType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.study.shop.domain.member.entity.QMember.member;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Repository
public class MemberQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public List<MemberListResponseDto> searchMembers(MemberSearchConditionDto cond) {
        return queryFactory
                .select(Projections.constructor(MemberListResponseDto.class,
                        member.email, member.nickname, member.role, member.deletedAt))
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

    public List<MemberListResponseDto> searchMembersNative(MemberSearchConditionDto cond) {
        StringBuilder sql = new StringBuilder("""
                SELECT email, nickname, role
                FROM member
                WHERE 1=1
                """);

        Map<String, Object> params = new LinkedHashMap<>();

        if (hasText(cond.getEmail())) {
            sql.append(" AND LOWER(email) LIKE LOWER(:email)");
            params.put("email", "%" + cond.getEmail() + "%");
        }
        if (cond.getRoleType() != null) {
            sql.append(" AND role = :role");
            params.put("role", cond.getRoleType().name());
        }
        if (cond.getIsDeleted() != null) {
            sql.append(cond.getIsDeleted()
                    ? " AND deleted_at IS NOT NULL"
                    : " AND deleted_at IS NULL");
            params.put("deleted_at", cond.getIsDeleted());
        }

        Query query = em.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> new MemberListResponseDto(
                        (String) row[0],
                        (String) row[1],
                        RoleType.valueOf((String) row[2]),
                        (LocalDateTime) row[3]
                ))
                .toList();
    }

}
