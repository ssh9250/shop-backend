package com.study.shop.admin.controller;

import com.study.shop.admin.service.OrderAdminService;
import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.global.enums.OrderStatus;
import com.study.shop.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Order Admin", description = "관리자 주문 관리 API")
public class OrderAdminController {
    private final OrderAdminService orderAdminService;

    @Operation(summary = "전체 주문 조회", description = "모든 주문을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.getAllOrders()));
    }

    @Operation(summary = "주문 단건 조회", description = "id를 통해 특정 주문을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.getOrderById(id)));
    }

    @Operation(summary = "주문 상태별 조회", description = "주문 상태(PENDING, ORDERED, IN_DELIVERY, COMPLETED, CANCELLED)로 필터링하여 조회합니다.")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.getOrdersByStatus(status)));
    }

    @Operation(summary = "회원별 주문 조회", description = "특정 회원의 주문 목록을 조회합니다.")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.getOrdersByMember(memberId)));
    }

    @Operation(summary = "주문 수락", description = "대기 중인 주문을 수락 처리합니다. (PENDING → ORDERED)")
    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<OrderResponseDto>> acceptOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.acceptOrder(id), "주문이 수락되었습니다."));
    }

    @Operation(summary = "배송 시작", description = "수락된 주문을 배송 중으로 변경합니다. (ORDERED → IN_DELIVERY)")
    @PatchMapping("/{id}/delivery")
    public ResponseEntity<ApiResponse<OrderResponseDto>> startDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.startDelivery(id), "배송이 시작되었습니다."));
    }

    @Operation(summary = "배송 완료", description = "배송 중인 주문을 완료 처리합니다. (IN_DELIVERY → COMPLETED)")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderResponseDto>> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.completeOrder(id), "주문이 완료되었습니다."));
    }

    @Operation(summary = "주문 강제 취소", description = "완료 전 주문을 관리자 권한으로 강제 취소합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderAdminService.cancelOrder(id), "주문이 취소되었습니다."));
    }
}