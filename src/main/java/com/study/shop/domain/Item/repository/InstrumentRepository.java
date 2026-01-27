package com.study.shop.domain.Item.repository;

import com.study.shop.domain.Item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<Item,Long> {
    List<Item> findBySellerId(Long sellerId);
}
