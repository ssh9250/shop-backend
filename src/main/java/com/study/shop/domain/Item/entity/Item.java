package com.study.shop.domain.Item.entity;

import com.study.shop.domain.Item.dto.UpdateItemRequestDto;
import com.study.shop.domain.category.entity.Category;
import com.study.shop.domain.category.entity.CategoryItem;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.enums.InstrumentCategory;
import com.study.shop.global.enums.ItemStatus;
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

    private int stock;
    private int price;
    private boolean used;
    private ItemStatus itemStatus;

    @OneToMany(mappedBy = "item")
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

    void assignMember(Member member) {
        this.seller = member;
        member.getItems().add(this);
    }

    public void validateOrderable() {
        if (!this.itemStatus.equals(ItemStatus.ON_SALE)) {
            throw new IllegalStateException("주문할 수 없는 상품입니다.");
        }
    }

    public static Item create(Member seller, String name, String description, int stock, int price, boolean used) {
        Item item = Item.builder()
                .name(name)
                .description(description)
                .stock(stock)
                .price(price)
                .used(used)
                .itemStatus(ItemStatus.ON_SALE)
                .build();
        item.assignMember(seller);

        return item;
    }

    public void update(UpdateItemRequestDto requestDto) {
        this.name = requestDto.getName();
        this.description = requestDto.getDescription();
        this.price = requestDto.getPrice();
        this.used = requestDto.isUsed();
        this.itemStatus = requestDto.getStatus();
    }

    public void addStock(int quantity) {
        this.stock += quantity;
    }
    public void removeStock(int quantity) {
        this.stock -= quantity;
    }
}
