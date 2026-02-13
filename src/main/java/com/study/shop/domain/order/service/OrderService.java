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
import com.study.shop.global.enums.RoleType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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


    // todo: 관리자 권한 admin으로 분리하기
    public void validateOrderAccess(Order order, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.getRole().equals(RoleType.ADMIN) && !order.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("주문에 접근할 권한이 없습니다.");
        }
    }

    public Long cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderAccess(order, memberId);
        order.cancel();
        return order.getId();
    }
}
