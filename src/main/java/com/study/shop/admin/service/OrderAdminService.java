package com.study.shop.admin.service;

import com.study.shop.domain.order.dto.OrderDetailDto;
import com.study.shop.domain.order.dto.OrderListDto;
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

    public List<OrderListDto> getAllOrders() {
        return orderRepository.findAll().stream().map(OrderListDto::from).collect(Collectors.toList());
    }

    public OrderDetailDto getOrderById(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        return OrderDetailDto.from(order);
    }

    public List<OrderListDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status).stream()
                .map(OrderListDto::from)
                .collect(Collectors.toList());
    }

    public List<OrderListDto> getOrdersByMember(Long memberId) {
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderListDto::from)
                .collect(Collectors.toList());
    }

    public OrderDetailDto acceptOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.accept();
        return OrderDetailDto.from(order);
    }

    public OrderDetailDto startDelivery(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.startDelivery();
        return OrderDetailDto.from(order);
    }

    public OrderDetailDto completeOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.complete();
        return OrderDetailDto.from(order);
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