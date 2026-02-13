package com.study.shop.domain.order.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {
    private List<CreateOrderItemRequestDto> orderItems;
    private String address;
}
