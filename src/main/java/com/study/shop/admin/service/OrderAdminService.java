package com.study.shop.admin.service;

import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.repository.OrderRepository;
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
}
