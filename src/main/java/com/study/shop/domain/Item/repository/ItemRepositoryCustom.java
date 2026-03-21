package com.study.shop.domain.Item.repository;

import com.study.shop.domain.Item.dto.ItemListDto;
import com.study.shop.domain.Item.dto.ItemSearchConditionDto;
import com.study.shop.domain.Item.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepositoryCustom {
    Slice<ItemListDto> findByCondition(ItemSearchConditionDto cond, LocalDateTime lastCreatedAt, Long lastId, Pageable pageable);
}
