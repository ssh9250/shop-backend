package com.study.shop.domain.order.dto;

import com.study.shop.global.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {
    private Long orderId;
    private Long memberId;

    private List<OrderItemDto> orderItemDtoList;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private String address;
}
