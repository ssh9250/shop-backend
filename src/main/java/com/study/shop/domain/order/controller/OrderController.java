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
    public ApiResponse<OrderResponseDto> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateOrderRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(OrderService.createOrder(requestDto)));
    }

    @Operation(summary = "전체 주문 조회", description = "모든 주문 정보를 조회합니다.")
    @GetMapping("/all")
    public ApiResponse<List<OrderResponseDto>> getAllOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 관리자용
        Long memberId = userDetails.getMemberId();
        return ResponseEntity.ok(ApiResponse.success(OrderService.getAllOrders(memberId)));

    }

    @Operation(summary = "주문 단건 조회", description = "id를 통해 특정 주문을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponseDto> getOrderById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 주문자가 본인이거나 관리자인 경우에만 조회 가능
    }

    @Operation(summary = "주문 생성", description = "주문을 생성합니다.")
    @PostMapping
    public ApiResponse<OrderResponseDto> createOrder(@RequestBody CreateOrderRequestDto requestDto) {}


}
