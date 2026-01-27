package com.study.shop.domain.Item.entity;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.enums.InstrumentCategory;
import com.study.shop.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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
    private String brand;
    private String description;

    private int price;
    private boolean used;
    private boolean available;

    @Enumerated(EnumType.STRING)
    private InstrumentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member seller;

    public void update(String name, String brand, String description, int price, boolean used, boolean available, InstrumentCategory category) {
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.used = used;
        this.available = available;
        this.category = category;
    }
}
