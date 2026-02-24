package com.study.shop.domain.order.service;

import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.exception.ItemNotFoundException;
import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.order.dto.CreateOrderRequestDto;
import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.entity.Order;
import com.study.shop.domain.order.entity.OrderItem;
import com.study.shop.domain.order.exception.OrderNotFoundException;
import com.study.shop.domain.order.repository.OrderRepository;
import com.study.shop.global.enums.OrderStatus;
import com.study.shop.global.enums.RoleType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    public OrderResponseDto createOrder(Long memberId, CreateOrderRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        List<OrderItem> orderItems = requestDto.getOrderItems().stream()
                .map(dto -> {
                    Item item = itemRepository.findById(dto.getItemId())
                            .orElseThrow(() -> new ItemNotFoundException(dto.getItemId()));
                    return OrderItem.create(item, dto.getQuantity());
                }).toList();

        Order order = Order.create(member, requestDto.getAddress());
        orderItems.forEach(order::addOrderItem);

        orderRepository.save(order);

        return OrderResponseDto.from(order);
    }

    public OrderResponseDto findOrderById(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOrderAccess(order, memberId);
        return OrderResponseDto.from(order);
    }

    public List<OrderResponseDto> getOrdersByStatus(Long memberId, OrderStatus status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        List<Order> orders = orderRepository.findByStatusAndMemberId(status, memberId);

        List<OrderResponseDto> responseDtos = new ArrayList<>();

        for (Order order : orders) {
            responseDtos.add(OrderResponseDto.from(order));
        }
//        orders.forEach(order -> {responseDtos.add(OrderResponseDto.from(order));});

        return responseDtos;
    }

    public OrderResponseDto acceptOrder(Long memberId, Long orderId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOrderAccess(order, memberId);

        order.accept();

        return OrderResponseDto.from(order);
    }

    public OrderResponseDto startDelivery(Long memberId, Long orderId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOrderAccess(order, memberId);
        order.startDelivery();
        return OrderResponseDto.from(order);
    }

    public OrderResponseDto completeOrder(Long memberId, Long orderId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderAccess(order, memberId);
        order.complete();
        return OrderResponseDto.from(order);
    }

    public Long cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderAccess(order, memberId);
        order.cancel();
        return order.getId();
    }

    public void validateOrderAccess(Order order, Long memberId) {
        if (!order.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("주문에 접근할 권한이 없습니다.");
        }
    }
}
