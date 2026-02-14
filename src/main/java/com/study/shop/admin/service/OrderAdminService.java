package com.study.shop.admin.service;

import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.entity.Order;
import com.study.shop.domain.order.exception.OrderNotFoundException;
import com.study.shop.domain.order.repository.OrderRepository;
import com.study.shop.global.enums.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderAdminService {
    private final OrderRepository orderRepository;

    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll().stream().map(OrderResponseDto::from).collect(Collectors.toList());
    }

    public OrderResponseDto getOrderById(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        return OrderResponseDto.from(order);
    }

    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDto> getOrdersByMember(Long memberId) {
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    public OrderResponseDto acceptOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.accept();
        return OrderResponseDto.from(order);
    }

    public OrderResponseDto startDelivery(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.startDelivery();
        return OrderResponseDto.from(order);
    }

    public OrderResponseDto completeOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.complete();
        return OrderResponseDto.from(order);
    }

    public Long cancelOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.forceCancel();
        return order.getId();
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}