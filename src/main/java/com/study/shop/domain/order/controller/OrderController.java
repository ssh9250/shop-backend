package com.study.shop.domain.order.controller;


import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.service.OrderService;
import com.study.shop.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/all")
    public ApiResponse<List<OrderResponseDto>> getAllOrders() {

    }

    @GetMapping("/{id}")
}
