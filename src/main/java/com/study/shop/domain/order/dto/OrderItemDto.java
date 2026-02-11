package com.study.shop.domain.order.dto;

import com.study.shop.domain.order.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemDto {
    private Long orderId;
    private Long orderItemId;
    private String ItemName;
    private Integer quantity;
    private BigDecimal price;

    public OrderItemDto from(OrderItem orderItem) {
        this.orderId = orderItem.getId();
        this.orderItemId = orderItem.getId();

    }
}
