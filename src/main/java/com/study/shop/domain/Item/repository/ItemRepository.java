package com.study.shop.domain.Item.repository;

import com.study.shop.domain.Item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,Long>, ItemRepositoryCustom {
    List<Item> findBySellerId(Long sellerId);

    @Query("select i " +
            "from Item i " +
            "join fetch i.seller m " +
            "where i.id = :itemId")
    Optional<Item> findItemByIdWithMember(Long itemId);
}
