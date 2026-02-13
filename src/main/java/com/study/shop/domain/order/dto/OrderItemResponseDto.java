package com.study.shop.domain.order.dto;

import com.study.shop.domain.order.entity.OrderItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDto {
    private Long orderId;
    private Long orderItemId;
    private Integer quantity;
    private int price;

    public static OrderItemResponseDto from(OrderItem orderItem) {
        return OrderItemResponseDto.builder()
                .orderId(orderItem.getOrder().getId())
                .orderItemId(orderItem.getId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}
