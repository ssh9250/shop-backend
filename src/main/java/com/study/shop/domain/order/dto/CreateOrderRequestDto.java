package com.study.shop.domain.order.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {
    private CreateOrderItemRequestDto orderItem;
    private String address;
}
