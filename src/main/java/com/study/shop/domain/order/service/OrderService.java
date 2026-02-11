package com.study.shop.domain.order.service;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.order.dto.CreateOrderRequestDto;
import com.study.shop.domain.order.dto.OrderResponseDto;
import com.study.shop.domain.order.entity.Order;
import com.study.shop.domain.order.repository.OrderRepository;
import com.study.shop.global.enums.RoleType;
import com.study.shop.global.security.auth.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    public OrderResponseDto createOrder(CreateOrderRequestDto requestDto) {
        Long orderId = requestDto.getOrderId();
    }

    public List<OrderResponseDto> getAllOrders(Long memberId) {
        if (!isAdmin(memberId)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }


    }

    public void validateOrderAccess(Order order, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.getRole().equals(RoleType.ADMIN) && !order.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("주문에 접근할 권한이 없습니다.");
        }
    }

    public Boolean isAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return member.getRole().equals(RoleType.ADMIN);
    }
}
