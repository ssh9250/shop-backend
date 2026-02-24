package com.study.shop.domain.order.controller;


import com.study.shop.domain.order.dto.CreateOrderRequestDto;
import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.service.OrderService;
import com.study.shop.global.enums.OrderStatus;
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

    // todo: 복잡한 로직 추가 + queryDSL 적용

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

    // *todo: 주문 로직 좀 더 살펴보기, n+1 문제 해결
    @Operation(summary = "주문 상태별 조회", description = "주문 상태(PENDING, ORDERED, IN_DELIVERY, COMPLETED, CANCELLED)로 필터링하여 조회합니다.")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByStatus(@PathVariable OrderStatus status, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByStatus(userDetails.getMemberId(), status)));
    }

    @Operation(summary = "주문 수락", description = "대기 중인 주문을 수락 처리합니다. (PENDING → ORDERED)")
    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<OrderResponseDto>> acceptOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(orderService.acceptOrder(userDetails.getMemberId(), id), "주문이 수락되었습니다."));
    }

    @Operation(summary = "배송 시작", description = "수락된 주문을 배송 중으로 변경합니다. (ORDERED → IN_DELIVERY)")
    @PatchMapping("/{id}/delivery")
    public ResponseEntity<ApiResponse<OrderResponseDto>> startDelivery(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(orderService.startDelivery(userDetails.getMemberId(), id), "배송이 시작되었습니다."));
    }

    @Operation(summary = "배송 완료", description = "배송 중인 주문을 완료 처리합니다. (IN_DELIVERY → COMPLETED)")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderResponseDto>> completeOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(orderService.completeOrder(userDetails.getMemberId(), id), "주문이 완료되었습니다."));
    }

    @Operation(summary = "주문 취소", description = "특정 주문을 취소합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> cancelOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long orderId = orderService.cancelOrder(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(orderId, "주문이 취소되었습니다."));
    }
}
