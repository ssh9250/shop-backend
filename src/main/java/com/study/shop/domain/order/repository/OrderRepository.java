package com.study.shop.domain.order.repository;

import com.study.shop.domain.order.entity.Order;
import com.study.shop.global.enums.OrderStatus;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByMemberId(Long memberId);

    @Query("select o " +
            "from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch o.member om " +
            "where o.orderStatus = :orderStatus " +
            "and om.id = :memberId")
    List<Order> findByOrderStatusAndMemberId(OrderStatus orderStatus, Long memberId);

    @Query("select o " +
            "from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch o.member om " +
            "where o.id = :orderId")
    Optional<Order> findById(@NonNull Long orderId);

    // findByOrderStatus -> orderitem, item, member 추가쿼리 발생 -> fetch join으로 해결
//queryFactory
//        .selectFrom(order)
//            .join(order.member, member).fetchJoin()
//    .join(order.orderItems, orderItem).fetchJoin()
//    .join(orderItem.item, item).fetchJoin()
//    .where(orderStatusEq(status), memberIdEq(memberId))
//            .fetch();
}
