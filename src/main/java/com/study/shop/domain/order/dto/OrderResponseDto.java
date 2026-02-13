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
public class OrderResponseDto {
    private Long orderId;
    private String memberEmail;
    private String memberNickname;

    private List<OrderItemResponseDto> orderItemDtoList;
    private OrderStatus orderStatus;
    private int totalPrice;
    private LocalDateTime orderDate;
    private String address;

    public static OrderResponseDto from(Order order) {
        List<OrderItemResponseDto> orderItemDtos = order.getOrderItems()
                .stream()
                .map(OrderItemResponseDto::from)
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .memberEmail(order.getMember().getEmail())
                .memberNickname(order.getMember().getNickname())
                .orderItemDtoList(orderItemDtos)
                .orderStatus(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getOrderDate())
                .address(order.getAddress())
                .build();
    }
}
