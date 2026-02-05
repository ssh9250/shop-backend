package com.study.shop.domain.category.entity;

import com.study.shop.domain.Item.entity.Item;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "category")
    private List<CategoryItem> categoryItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    // 패키지 프라이빗 메서드
    void changeParent(Category category) {
        this.parent = category;
    }

    public void addChild(Category child) {
        this.child.add(child);
        child.changeParent(child);
    }

    public void removeChild(Category child) {
        this.child.remove(child);
        child.changeParent(null);
    }

    public void addItem(Item item) {
        CategoryItem itemCategory = CategoryItem.builder()
                .category(this)
                .item(item)
                .build();
        if (!this.categoryItems.contains(itemCategory)){
            this.categoryItems.add(itemCategory);
            item.getCategoryItems().add(itemCategory);
        }
    }

    public void removeItem(Item item) {
        this.categoryItems.removeIf(ci -> ci.getItem().equals(item));
        item.getCategoryItems().removeIf(ci -> ci.getCategory().equals(this));
    }
}