package com.study.shop.domain.order.repository;

import com.study.shop.domain.order.entity.Order;
import com.study.shop.global.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByMemberId(Long memberId);

    List<Order> findByOrderStatusAndMemberId(OrderStatus orderStatus, Long memberId);

    // findByOrderStatus -> orderitem, item, member 추가쿼리 발생 -> fetch join으로 해결
//queryFactory
//        .selectFrom(order)
//            .join(order.member, member).fetchJoin()
//    .join(order.orderItems, orderItem).fetchJoin()
//    .join(orderItem.item, item).fetchJoin()
//    .where(orderStatusEq(status), memberIdEq(memberId))
//            .fetch();
}
