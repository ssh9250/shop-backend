package com.study.shop.domain.Item.entity;

import com.study.shop.domain.Item.dto.UpdateItemRequestDto;
import com.study.shop.domain.category.entity.Category;
import com.study.shop.domain.category.entity.CategoryItem;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.enums.InstrumentCategory;
import com.study.shop.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Item extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
//    private String brand;
    private String description;

    private int price;
    private boolean used;
    private boolean available;

    @OneToMany(mappedBy = "items")
    private List<CategoryItem> categoryItems =  new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Member seller;

    public void addCategory(Category category) {
        CategoryItem categoryItem = CategoryItem.builder()
                .category(category)
                .item(this)
                .build();
        if  (!this.categoryItems.contains(categoryItem)){
            this.categoryItems.add(categoryItem);
            category.getCategoryItems().add(categoryItem);
        }
    }

    public void removeCategory(Category category) {
        categoryItems.removeIf(ci -> ci.getCategory().equals(category));
        category.getCategoryItems().removeIf(ci -> ci.getCategory().equals(this));
    }

    public void update(UpdateItemRequestDto requestDto) {
        this.name = requestDto.getName();
        this.description = requestDto.getDescription();
        this.price = requestDto.getPrice();
        this.used = requestDto.isUsed();
        this.available = requestDto.isAvailable();
    }
}
