package com.study.shop.domain.order.controller;


import com.study.shop.domain.order.dto.CreateOrderRequestDto;
import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.service.OrderService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "주문을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateOrderRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(userDetails.getMemberId(), requestDto)));
    }

    @Operation(summary = "주문 단건 조회", description = "id를 통해 특정 주문을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(orderService.findOrderById(userDetails.getMemberId(), id)));
    }

    @Operation(summary = "주문 취소", description = "id를 통해 특정 주문을 취소 처리합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> cancelOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long orderId = orderService.cancelOrder(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(orderId, "주문이 취소되었습니다."));
    }
}
