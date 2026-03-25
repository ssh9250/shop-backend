package com.study.shop.domain.order.dto;

import com.study.shop.domain.order.entity.Order;
import com.study.shop.global.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListDto {
    private Long orderId;
    private String memberEmail;

    private OrderStatus orderStatus;
    private int totalPrice;
    private LocalDateTime orderDate;
    private String address;

    public static OrderListDto from(Order order) {
        return OrderListDto.builder()
                .orderId(order.getId())
                .memberEmail(order.getMember().getEmail())
                .orderStatus(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getOrderDate())
                .address(order.getAddress())
                .build();
    }
}
