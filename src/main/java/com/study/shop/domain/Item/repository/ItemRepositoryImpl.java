package com.study.shop.domain.Item.repository;

import com.study.shop.domain.Item.dto.ItemSearchConditionDto;
import com.study.shop.domain.Item.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public class ItemRepositoryImpl implements ItemRepositoryCustom {
    public Slice<Item> findByCondition(ItemSearchConditionDto cond, Long lastId, Pageable pageable) {
        return null;
    }
    // todo*: item slice 구현

}
