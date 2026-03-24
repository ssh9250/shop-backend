package com.study.shop.domain.Item.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.shop.domain.Item.dto.ItemListDto;
import com.study.shop.domain.Item.dto.ItemSearchConditionDto;
import com.study.shop.domain.Item.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.study.shop.domain.Item.entity.QItem.item;
import static com.study.shop.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public Slice<ItemListDto> findByCondition(ItemSearchConditionDto cond, LocalDateTime lastCreatedAt, Long lastId, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        List<ItemListDto> content = queryFactory
                .select(Projections.constructor(ItemListDto.class,
                        item.id, item.name, item.stock, item.price, item.used, member.nickname, item.createdAt))
                .from(item)
                .join(item.seller, member)
                .where(cursorCondition(lastCreatedAt, lastId))
                .orderBy(item.createdAt.desc(), item.id.desc())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = content.size() > pageSize;

        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression cursorCondition(LocalDateTime lastCreatedAt, Long lastId) {
        if (lastCreatedAt == null || lastId == null) {
            return null;
        }
        return item.createdAt.lt(lastCreatedAt)
                .or(item.createdAt.eq(lastCreatedAt))
                .and(item.id.lt(lastId));
    }
}
