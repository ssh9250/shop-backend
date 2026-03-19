package com.study.shop.domain.Item.repository;

import com.study.shop.domain.Item.dto.ItemSearchConditionDto;
import com.study.shop.domain.Item.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ItemRepositoryCustom {
    Slice<Item> findByCondition(ItemSearchConditionDto cond, Long lastId, Pageable pageable);
}
