package com.study.shop.domain.order.entity;

import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.order.exception.StockNotEnoughException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private Integer quantity;

    // 주문 당시의 가격
    private int price;

    // 패키지 프라이빗
    void assignOrder(Order order) {
        this.order = order;
    }

    // 생성 메서드
    public static OrderItem create(Item item, Integer quantity) {
        item.validateOrderable();

        if (item.getStock() < quantity) {
            throw new StockNotEnoughException(item.getId());
        }
        item.removeStock(quantity);
        return OrderItem.builder()
                .item(item)
                .quantity(quantity)
                .price(item.getPrice())
                .build();
    }

    // 비즈니스 로직
    public void cancel(){
        getItem().addStock(quantity);
    }

    // 조회 로직
    public int getTotalPrice() {
        return price * quantity;
    }
}