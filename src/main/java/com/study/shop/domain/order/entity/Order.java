package com.study.shop.domain.order.entity;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;
    private int totalPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private String address;

    void assignMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        // 패키지 프라이빗
        orderItem.assignOrder(this);
    }

    private void calculateTotalPrice() {
        this.totalPrice = this.orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }

    // 생성 메서드
    public static Order create(Member member, String address) {
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.PENDING)
                .address(address)
                .build();

        order.assignMember(member);
        order.calculateTotalPrice();

        return order;
    }

    // 비즈니스 로직
    public void cancel() {
        if (this.orderStatus != OrderStatus.PENDING){
            throw new IllegalStateException("주문 수락 전에만 취소할 수 있습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    public void accept() {
        validateStatusTransition(OrderStatus.PENDING, OrderStatus.ORDERED);
        this.orderStatus = OrderStatus.ORDERED;
    }

    public void startDelivery() {
        validateStatusTransition(OrderStatus.ORDERED, OrderStatus.IN_DELIVERY);
        this.orderStatus = OrderStatus.IN_DELIVERY;
    }

    public void complete() {
        validateStatusTransition(OrderStatus.IN_DELIVERY, OrderStatus.COMPLETED);
        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void forceCancel() {
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        if (this.orderStatus == OrderStatus.COMPLETED) {
            throw new IllegalStateException("완료된 주문은 취소할 수 없습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    private void validateStatusTransition(OrderStatus expected, OrderStatus next) {
        if (this.orderStatus != expected) {
            throw new IllegalStateException(
                    String.format("주문 상태를 %s(으)로 변경할 수 없습니다. 현재 상태: %s", next, this.orderStatus)
            );
        }
    }
}
