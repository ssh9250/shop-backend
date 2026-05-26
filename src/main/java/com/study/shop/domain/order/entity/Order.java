package com.study.shop.domain.order.entity;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.order.exception.InvalidOrderStatusException;
import com.study.shop.domain.order.exception.OrderNotFoundException;
import com.study.shop.global.enums.OrderStatus;
import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;
import com.study.shop.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at is NULL")
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 주문 시점 스냅샷
    private Long sellerId;
    private String buyerEmail;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private OrderItem orderItem;

    private LocalDateTime orderDate;
    private int totalPrice;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;
    private String address;

    void assignMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
        // 패키지 프라이빗
        orderItem.assignOrder(this);
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        this.totalPrice = this.orderItem.getTotalPrice();
    }

    // 생성 메서드
    public static Order create(Member member, String address, Long sellerId) {
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.PENDING)
                .address(address)
                .sellerId(sellerId)
                .buyerEmail(member.getEmail())
                .build();

        order.assignMember(member);

        return order;
    }

    // 비즈니스 로직
    public void cancel() {
        if (!this.orderStatus.canCancel()) {
            throw InvalidOrderStatusException.cannotCancel();
        }
        this.orderStatus = OrderStatus.CANCELLED;
        orderItem.cancel();
    }

    public void accept() {
        this.orderStatus = this.orderStatus.next();
    }

    public void startDelivery() {
        this.orderStatus = this.orderStatus.next();
    }

    public void complete() {
        this.orderStatus = this.orderStatus.next();
    }

    // 관리자 전용 로직
    public void forceCancel() {
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        if (this.orderStatus == OrderStatus.COMPLETED) {
            throw new IllegalStateException("완료된 주문은 취소할 수 없습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        orderItem.cancel();
    }
}
