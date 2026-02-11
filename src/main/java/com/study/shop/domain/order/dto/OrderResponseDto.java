package com.study.shop.domain.order.dto;

import com.study.shop.domain.order.entity.Order;
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
public class OrderResponseDto {
    private Long orderId;
    private String memberEmail;
    private String memberNickname;

    private List<OrderItemDto> orderItemDtoList;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private String address;

    public OrderResponseDto from(Order order) {
        this.orderId = order.getId();
        this.memberEmail = order.getMember().getEmail();
        this.memberNickname = order.getMember().getNickname();

    }


}
