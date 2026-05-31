package com.study.shop.domain.order.service;

import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.exception.ItemNotFoundException;
import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.event.OrderAcceptedEvent;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.order.dto.CreateOrderRequestDto;
import com.study.shop.domain.order.dto.OrderDetailDto;
import com.study.shop.domain.order.dto.OrderListDto;
import com.study.shop.domain.order.entity.Order;
import com.study.shop.domain.order.entity.OrderItem;
import com.study.shop.domain.order.exception.InvalidOrderException;
import com.study.shop.domain.order.exception.OrderNotFoundException;
import com.study.shop.domain.order.exception.StockNotEnoughException;
import com.study.shop.domain.order.repository.OrderRepository;
import com.study.shop.global.enums.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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
    private final ApplicationEventPublisher eventPublisher;

//        @Retryable(
//            retryFor = {ObjectOptimisticLockingFailureException.class,
//                    CannotAcquireLockException.class},
//            noRetryFor = {StockNotEnoughException.class, ItemNotFoundException.class, MemberNotFoundException.class},
//            maxAttempts = 5,
//            backoff = @Backoff(delay = 100)
//    )
    public OrderDetailDto createOrder(Long memberId, CreateOrderRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

//                     비관적 락 적용을 위한 코드
            Item item = itemRepository.findByIdWithLock(requestDto.getOrderItem().getItemId())

//        Item item = itemRepository.findById(requestDto.getOrderItem().getItemId())
                .orElseThrow(() -> new ItemNotFoundException(requestDto.getOrderItem().getItemId()));

        if (item.getSeller().getId().equals(member.getId())) {
            throw InvalidOrderException.selfPurchaseException();
        }

        OrderItem orderItem = OrderItem.create(item, requestDto.getOrderItem().getQuantity());

        Order order = Order.create(member, requestDto.getAddress(), item.getSeller().getId());
        order.addOrderItem(orderItem);

        orderRepository.save(order);

        return OrderDetailDto.from(order);
    }


    public List<OrderListDto> getOrderList(Long memberId) {
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderListDto::from)
                .collect(Collectors.toList());
    }

    public OrderDetailDto findOrderById(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        validateBuyer(order, memberId);
        return OrderDetailDto.from(order);
    }

    public List<OrderListDto> getSoldOrderList(Long memberId) {
        return orderRepository.findBySellerId(memberId).stream()
                .map(OrderListDto::from)
                .collect(Collectors.toList());
    }

    public OrderDetailDto findSoldOrderById(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        validateSeller(order, memberId);
        return OrderDetailDto.from(order);
    }

    public List<OrderListDto> getOrdersByStatus(Long memberId, OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatusAndMemberId(status, memberId);

        List<OrderListDto> responseDtos = new ArrayList<>();

        for (Order order : orders) {
            responseDtos.add(OrderListDto.from(order));
        }
//        orders.forEach(order -> {responseDtos.add(OrderResponseDto.from(order));});

        return responseDtos;
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            noRetryFor = {StockNotEnoughException.class, ItemNotFoundException.class, MemberNotFoundException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public OrderDetailDto acceptOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderStateAccess(order, memberId);

        order.accept();
        eventPublisher.publishEvent(new OrderAcceptedEvent(orderId, order.getOrderItem().getItem().getId()));
//        order.getOrderItem().getItem().reserveOrSellOut();

        return OrderDetailDto.from(order);
    }

    public OrderDetailDto startDelivery(Long memberId, Long orderId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOrderStateAccess(order, memberId);
        order.startDelivery();
        return OrderDetailDto.from(order);
    }

    public OrderDetailDto completeOrder(Long memberId, Long orderId) {
        // 불필요한 검증 로직 (JWT를 통해 회원이 존재한다는 것이 보장됨)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderStateAccess(order, memberId);
        order.complete();
        return OrderDetailDto.from(order);
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            noRetryFor = {StockNotEnoughException.class, ItemNotFoundException.class, MemberNotFoundException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Long cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderAccess(order, memberId);
        order.cancel();
        return order.getId();
    }


    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            noRetryFor = {StockNotEnoughException.class, ItemNotFoundException.class, MemberNotFoundException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Long rejectOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderStateAccess(order, memberId);
        order.cancel();
        return order.getId();
    }

    public void validateOrderAccess(Order order, Long memberId) {
        if (!order.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("주문에 접근할 권한이 없습니다.");
        }
    }

    public void validateOrderStateAccess(Order order, Long memberId) {
        if (!order.getSellerId().equals(memberId)) {
            throw new AccessDeniedException("주문 상태를 변경할 권한이 없습니다.");
        }
    }

    public void validateBuyer(Order order, Long memberId) {
        if (!order.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("주문에 접근할 권한이 없습니다.");
        }
    }

    public void validateSeller(Order order, Long memberId) {
        if (!order.getSellerId().equals(memberId)) {
            throw new AccessDeniedException("주문에 접근할 권한이 없습니다.");
        }
    }


    @Recover
    public OrderDetailDto recoverCreateOrder(RuntimeException e, Long memberId, CreateOrderRequestDto requestDto) {
//        if (e instanceof CannotAcquireLockException) {
//            throw new OptimisticLockingFailureException("주문 요청이 많아 처리중입니다. 잠시만 기다려주세요.");
//        }
        throw e;
    }

    @Recover
    public OrderDetailDto RecoverAcceptOrder(RuntimeException e, Long memberId, Long orderId) {
        throw e;
    }

    @Recover
    public Long recoverCancelOrder(RuntimeException e, Long memberId, Long orderId) {
        throw e;
    }

    @Recover
    public Long recoverRejectOrder(RuntimeException e, Long memberId, Long orderId) {
        throw e;
    }
}