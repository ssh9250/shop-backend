package com.study.shop.domain.category.entity;

import com.study.shop.domain.Item.entity.Item;
import jakarta.persistence.*;

@Entity
public class CategoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private CategoryItem parent;
    @ManyToOne(fetch = FetchType.LAZY)
    private CategoryItem child;

    @ManyToOne
    private Item item;
}
