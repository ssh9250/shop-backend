package com.study.shop.domain.order.controller;


import com.study.shop.domain.order.dto.CreateOrderRequestDto;
import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.service.OrderService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ApiResponse<List<OrderResponseDto>> getAllOrders(@AuthenticationPrincipal CustomUserDetails userDetails, CreateOrderRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(OrderService.createOrder(requestDto)));

    }

    @Operation(summary = "주문 단건 조회", description = "id를 통해 특정 주문을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponseDto> getOrder()
}
