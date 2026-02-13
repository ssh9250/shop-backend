package com.study.shop.domain.order.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderItemRequestDto {
    private Long itemId;
    private Integer quantity;
    private int price;
}
