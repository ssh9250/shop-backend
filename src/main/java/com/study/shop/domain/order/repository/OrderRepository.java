package com.study.shop.domain.order.repository;

import com.study.shop.domain.order.entity.Order;
import com.study.shop.global.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByMemberId(Long memberId);
}
