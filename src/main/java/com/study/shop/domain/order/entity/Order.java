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
}
